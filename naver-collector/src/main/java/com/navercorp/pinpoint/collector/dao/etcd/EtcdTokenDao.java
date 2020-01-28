/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.etcd;

import com.navercorp.pinpoint.collector.config.TokenConfig;
import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.etcd.EtcdClient;
import com.navercorp.pinpoint.collector.vo.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class EtcdTokenDao implements TokenDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EtcdClient etcdClient;

    private final String path;
    private final long ttl;

    public EtcdTokenDao(TokenConfig tokenConfig) {
        this.etcdClient = new EtcdClient(tokenConfig.getAddress());
        Objects.requireNonNull(tokenConfig, "tokenConfig");

        String path = tokenConfig.getPath();
        Objects.requireNonNull(path, "path");

        if (!path.endsWith("/")) {
            this.path = path + "/";
        } else {
            this.path = path;
        }

        long ttlMillis = tokenConfig.getTtl();
        if (ttlMillis < 0) {
            this.ttl = -1;
        } else {
            this.ttl = TimeUnit.MILLISECONDS.toSeconds(ttlMillis) + 1;
        }
    }

    @Override
    public boolean create(Token token) {
        try {
            byte[] jsonBytes = token.toJson();
            etcdClient.put(path + token.getKey(), jsonBytes, ttl);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to create token. Caused:{}", e.getMessage(), e);
        }

        return false;
    }

    @Override
    public Token getAndRemove(String tokenKey) {
        try {
            byte[] prevValue = etcdClient.delete(path + tokenKey);
            if (prevValue == null) {
                logger.info("Can not find value");
                return null;
            }

            return Token.toObject(prevValue);
        } catch (Exception e) {
            logger.warn("Failed to getAndRemove token({}). Caused:{}", tokenKey, e.getMessage(), e);
        }
        return null;
    }

}
