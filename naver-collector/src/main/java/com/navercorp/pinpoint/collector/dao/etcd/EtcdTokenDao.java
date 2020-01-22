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

import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.etcd.EtcdClient;
import com.navercorp.pinpoint.collector.service.TokenConfig;
import com.navercorp.pinpoint.collector.vo.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class EtcdTokenDao implements TokenDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TokenConfig tokenConfig;

    private final EtcdClient etcdClient;

    public EtcdTokenDao(String etcdAddress, TokenConfig tokenConfig) {
        this.etcdClient = new EtcdClient(etcdAddress);
        this.tokenConfig = Objects.requireNonNull(tokenConfig, "tokenConfig");
    }

    @Override
    public boolean create(Token token) {
        try {
            byte[] jsonBytes = token.toJson();
            etcdClient.put(token.getKey(), jsonBytes, tokenConfig.getTtl());
            return true;
        } catch (Exception e) {
            logger.warn("Failed to create token. Caused:{}", e.getMessage(), e);
        }

        return false;
    }

    @Override
    public Token getAndRemove(String tokenKey) {
        try {
            byte[] prevValue = etcdClient.delete(tokenKey);
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
