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
import com.navercorp.pinpoint.collector.vo.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Taejin Koo
 */
@Service("tokenService")
@Profile("tokenAuthentication")
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private TokenDao tokenDao;

    @Override
    public Token create(TokenCreateRequest request) {
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
        return tokenDao.getAndRemove(tokenKey);
    }

}
