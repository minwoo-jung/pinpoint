/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.service.TokenConfig;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.security.util.ExpiredTaskManager;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Repository
@Profile("tokenAuthentication")
public class ZookeeperTokenDao implements TokenDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Object lock = new Object();

    @Autowired
    private TokenConfig tokenConfig;

    private String parentPath;

    private ZookeeperClient client;

    private Timer timer;
    private ExpiredTaskManager<String> tokenLifeCycleManager;

    @PostConstruct
    public void start() throws IOException {
        logger.info("start() started. config:{}", tokenConfig);

        ZookeeperWatcher watcher = new ZookeeperWatcher();

        String parentPath = tokenConfig.getPath();
        if (parentPath.endsWith(ZookeeperConstants.PATH_SEPARATOR)) {
            this.parentPath = parentPath;
        } else {
            this.parentPath = parentPath + ZookeeperConstants.PATH_SEPARATOR;
        }

        this.client = new CuratorZookeeperClient(tokenConfig.getAddress(), tokenConfig.getSessionTimeout(), watcher);
        this.client.connect();

        this.timer = createTimer();
        this.tokenLifeCycleManager = new ExpiredTaskManager<>(timer, tokenConfig.getTtl());
    }

    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-ExpiredTaskManager-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    @PreDestroy
    public void stop() {
        logger.info("stop() started.");

        if (timer != null) {
            timer.stop();
        }

        if (client != null) {
            this.client.close();
        }
    }

    @Override
    public boolean create(Token token) {
        if (!createTokenRootPath(parentPath)) {
            return false;
        }
        return createTokenNode(token);
    }

    private boolean createTokenRootPath(String path) {
        try {
            client.createPath(path);
            return true;
        } catch (Exception e) {
            logger.warn("failed to create token root path");
        }

        return false;
    }

    private boolean createTokenNode(Token token) {
        String tokenKey = token.getKey();
        String fullPath = ZKPaths.makePath(parentPath, tokenKey);

        long expiredTime = token.getExpiryTime() - System.currentTimeMillis();

        if (expiredTime <= 0) {
            return false;
        } else {
            try {
                byte[] payload = OBJECT_MAPPER.writeValueAsBytes(token);
                CreateNodeMessage createNodeMessage = new CreateNodeMessage(fullPath, payload);
                client.createNode(createNodeMessage);

                tokenLifeCycleManager.reserve(tokenKey, expiredTime, new FutureListener<Boolean>() {

                    @Override
                    public void onComplete(Future<Boolean> future) {
                        if (future.isSuccess() && future.getResult()) {
                            return;
                        } else {
                            boolean deleteResult = deleteNode(tokenKey);
                            if (!deleteResult) {
                                tokenLifeCycleManager.failed(tokenKey);
                                tokenLifeCycleManager.reserve(tokenKey, tokenConfig.getOperationRetryInterval(), this);
                            }
                        }
                    }

                });
                return true;
            } catch (Exception e) {
                logger.warn("failed to create token path:{}", fullPath, e);
            }
        }

        return false;
    }

    @Override
    public Token getAndRemove(String tokenKey) {
        String fullPath = ZKPaths.makePath(parentPath, tokenKey);

        byte[] payload = null;
        try {
            payload = client.getData(fullPath);
        } catch (Exception e) {
            logger.debug("failed to get token data:{}", tokenKey);
        }

        boolean result = deleteNode(tokenKey);
        if (result) {
            tokenLifeCycleManager.succeed(tokenKey);
        }

        if (payload == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(payload, Token.class);
        } catch (IOException e) {
            logger.warn("failed to deserialize. tokenKey:{}", tokenKey);
        }
        return null;
    }

    boolean deleteNode(String tokenKey) {
        String fullPath = ZKPaths.makePath(parentPath, tokenKey);

        synchronized (lock) {
            try {
                client.delete(fullPath);
                return true;
            } catch (Exception e) {
                logger.debug("failed to delete token data:{}", tokenKey);
            }
            return false;
        }
    }

    private class ZookeeperWatcher implements ZookeeperEventWatcher {

        private List<String> registeredTokenList = new ArrayList<>();

        @Override
        public void process(WatchedEvent event) {
            logger.debug("Process Zookeeper Event({})", event);

            Event.EventType eventType = event.getType();
            if (client.isConnected()) {
                if (eventType == Event.EventType.NodeChildrenChanged) {
                    handleChildrenChanged();
                }
            }
        }

        @Override
        public boolean handleConnected() {
            try {
                getTokenData();
                return true;
            } catch (Exception e) {
                logger.info("failed while to execute handleConnected(). message:{}", e.getMessage(), e);
            }
            return false;
        }

        @Override
        public boolean handleDisconnected() {
            return true;
        }


        private void handleChildrenChanged() {
            getTokenData();
        }

        private boolean getTokenData() {
            logger.info("syncPullCollectorCluster() started.");
            synchronized (lock) {
                this.registeredTokenList = getTokenData0(client, tokenConfig.getPath(), registeredTokenList);
                return true;
            }
        }

        private List<String> getTokenData0(ZookeeperClient client, String path, List<String> localTokenList) {
            try {
                logger.info("getTokenData0() started");

                List<String> zookeeperTokenList = new ArrayList<>(client.getChildNodeList(path, true));
                if (CollectionUtils.isEmpty(zookeeperTokenList)) {
                    return Collections.emptyList();
                }

                List<String> newTokenList = new ArrayList<>();
                for (String tokenKey : localTokenList) {
                    if (zookeeperTokenList.remove(tokenKey)) {
                        newTokenList.add(tokenKey);
                    } else {
                        // expected already deleted
                        tokenLifeCycleManager.succeed(tokenKey);
                    }
                }

                newTokenList.addAll(zookeeperTokenList);

                logger.info("getTokenData0() completed");

                return newTokenList;
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }

            return localTokenList;
        }

    }

}