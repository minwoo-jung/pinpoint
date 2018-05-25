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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.service.LoginService;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author Taejin Koo
 */
@Service("createTokenHandler")
@Profile("tokenAuthentication")
public class CreateTokenHandler implements RequestResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LoginService loginService;

    @Override
    public TBase<?, ?> handleRequest(TBase<?, ?> tbase) {
        if (tbase instanceof TCmdGetAuthenticationToken) {
            TokenCreateRequest tokenCreateRequest = createTokenCreateRequest((TCmdGetAuthenticationToken) tbase);
            if (tokenCreateRequest == null) {
                return createResponse(TTokenResponseCode.BAD_REQUEST);
            }
            if (!loginService.login(tokenCreateRequest.getUserId(), tokenCreateRequest.getPassword())) {
                return createResponse(TTokenResponseCode.UNAUTHORIZED);
            }

            TCmdGetAuthenticationTokenRes response = null;
            try {
                Token token = tokenService.create(tokenCreateRequest);
                response = createResponse(token.getKey());
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                response = createResponse(TTokenResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return response;
        } else {
            return createResponse(TTokenResponseCode.BAD_REQUEST);
        }
    }

    private TCmdGetAuthenticationTokenRes createResponse(String token) {
        TCmdGetAuthenticationTokenRes response = new TCmdGetAuthenticationTokenRes();
        response.setCode(TTokenResponseCode.OK);
        response.setToken(token.getBytes());
        return response;
    }

    private TCmdGetAuthenticationTokenRes createResponse(TTokenResponseCode responseCode) {
        return createResponse(responseCode, responseCode.name());
    }

    private TCmdGetAuthenticationTokenRes createResponse(TTokenResponseCode responseCode, String message) {
        TCmdGetAuthenticationTokenRes response = new TCmdGetAuthenticationTokenRes();
        response.setCode(responseCode);
        response.setMessage(message);
        return response;
    }

    private TokenCreateRequest createTokenCreateRequest(TCmdGetAuthenticationToken request) {
        try {
            TokenCreateRequest tokenCreateRequest = new TokenCreateRequest(request.getUserId(), request.getPassword(), request.getTokenType());
            return tokenCreateRequest;
        } catch (Exception e) {
            // skip
        }
        return null;
    }

}
