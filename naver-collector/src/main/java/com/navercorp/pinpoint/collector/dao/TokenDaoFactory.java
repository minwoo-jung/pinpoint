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

package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.collector.config.TokenConfig;
import com.navercorp.pinpoint.collector.dao.etcd.EtcdTokenDao;
import com.navercorp.pinpoint.collector.dao.memory.MemoryTokenDao;

/**
 * @author Taejin Koo
 */
public class TokenDaoFactory {

    public static TokenDao create(TokenConfig tokenConfig) {
        TokenDaoType tokenDaoType = TokenDaoType.getType(tokenConfig.getTokenTypeName());
        return tokenDaoType.getTokenDao(tokenConfig);
    }

    private enum TokenDaoType {

        NONE {
            @Override
            TokenDao getTokenDao(TokenConfig tokenConfig) {
                return new DisabledTokenDao();
            }
        },

        MEMORY {
            @Override
            TokenDao getTokenDao(TokenConfig tokenConfig) {
                return new MemoryTokenDao();
            }
        },

        ETCD {
            @Override
            TokenDao getTokenDao(TokenConfig tokenConfig) {
                return new EtcdTokenDao(tokenConfig);
            }
        };

        private static TokenDaoType getType(String daoTypeName) {
            if (daoTypeName == null) {
                return NONE;
            }

            for (TokenDaoType daoType : TokenDaoType.values()) {
                if (daoTypeName.equalsIgnoreCase(daoType.name())) {
                    return daoType;
                }
            }

            return NONE;
        }

        abstract TokenDao getTokenDao(TokenConfig tokenConfig);

    }

}
