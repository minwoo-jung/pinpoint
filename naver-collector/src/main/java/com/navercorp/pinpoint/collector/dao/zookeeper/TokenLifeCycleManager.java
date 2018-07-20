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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
class TokenLifeCycleManager {

    // Currently have implemented ttl using ephemeral node and TimerTask.
    // If later use Zookeeper with ttl support (3.5.3), change to the Zookeeper node.

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Map<String, Timeout> willBeDeleteTokenMap = new ConcurrentHashMap<>();

    private final ZookeeperTokenDao zookeeperTokenDao;

    private final long retryIntervalMillis;

    private final Timer timer;

    TokenLifeCycleManager(ZookeeperTokenDao zookeeperTokenDao, long retryIntervalMillis) {
        this.zookeeperTokenDao = Assert.requireNonNull(zookeeperTokenDao, "zookeeperTokenDao must not be null");

        Assert.isTrue(retryIntervalMillis > 0, "retryIntervalMillis must be greater than 0");
        this.retryIntervalMillis = retryIntervalMillis;

        this.timer = createTimer();
    }

    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-TokenLifeCycleManager-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    void delete(String tokenKey) {
        delete0(tokenKey);
    }

    void reserveDeleteTask(String tokenKey, long timeoutMillis) {
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be greater than 0");
        }

        reserveDeleteTask0(tokenKey, timeoutMillis);
    }

    private void reserveDeleteTask0(String tokenKey, long timeoutMillis) {
        if (isDebug) {
            logger.debug("reserveDeleteTask0() started. token:{}, execution delay:{}", tokenKey, timeoutMillis);
        }

        Timeout timeout = timer.newTimeout(new DeleteNodeTask(tokenKey), timeoutMillis, TimeUnit.MILLISECONDS);
        willBeDeleteTokenMap.put(tokenKey, timeout);
    }

    void cancelReserveDeleteTask(String tokenKey) {
        Timeout timeout = willBeDeleteTokenMap.remove(tokenKey);
        if (timeout != null) {
            timeout.cancel();
        }
    }

    private void delete0(String tokenKey) {
        logger.info("delete0() started. token:{}", tokenKey);

        boolean deleted = zookeeperTokenDao.deleteNode(tokenKey);
        if (!deleted) {
            reserveDeleteTask0(tokenKey, retryIntervalMillis);
        } else {
            // cancel
            cancelReserveDeleteTask(tokenKey);
            logger.info("delete0() succeed. token:{}", tokenKey);
        }
    }

    @VisibleForTesting
    Map<String, Timeout> getWillBeDeleteTokenMap() {
        return willBeDeleteTokenMap;
    }

    private class DeleteNodeTask implements TimerTask {

        private final String tokenKey;

        private DeleteNodeTask(String tokenKey) {
            this.tokenKey = Assert.requireNonNull(tokenKey, "tokenKey must not be null");
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (isDebug) {
                logger.debug("{} started. token;{}", ClassUtils.simpleClassName(this), tokenKey);
            }
            if (timeout.isCancelled()) {
                return;
            }

            delete0(tokenKey);
        }

    }

}
