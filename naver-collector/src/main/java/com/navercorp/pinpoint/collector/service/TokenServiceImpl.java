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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.grpc.security.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Taejin Koo
 */
@Service("tokenService")
@Profile("tokenAuthentication")
public class TokenServiceImpl implements TokenService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TokenConfig tokenConfig;
    private final TokenDao tokenDao;

    @Autowired
    public TokenServiceImpl(TokenConfig tokenConfig, TokenDao tokenDao) {
        this.tokenConfig = Objects.requireNonNull(tokenConfig, "tokenConfig");
        this.tokenDao = Objects.requireNonNull(tokenDao, "tokenDao");
    }

    @Override
    public Token create(TokenCreateRequest request) {
        if (isDebug) {
            logger.debug("create() started");
        }

        for (int i = 0; i < tokenConfig.getMaxRetryCount(); i++) {
            Token token = createToken(request);
            if (tokenDao.create(token)) {
                return token;
            }
        }
        return null;
    }

    private Token createToken(TokenCreateRequest request) {
        String tokenKey = UUID.randomUUID().toString();

        PaaSOrganizationInfo paaSOrganizationInfo = request.getPaaSOrganizationInfo();

        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + tokenConfig.getTtl();

        return new Token(tokenKey, paaSOrganizationInfo, expiryTime, request.getRemoteAddress(), request.getTokenType());
    }

    @Override
    public Token getAndRemove(String tokenKey, TokenType tokenType) {
        if (isDebug) {
            logger.debug("getAndRemove() started");
        }

        if (tokenKey == null) {
            return null;
        }

        return tokenDao.getAndRemove(tokenKey);
    }

}
