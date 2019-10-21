/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PAuthCode;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PSecurityResult;
import com.navercorp.pinpoint.grpc.security.TokenType;
import com.navercorp.pinpoint.grpc.security.client.AuthorizationTokenProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class TestTokenProvider implements AuthorizationTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthGrpc.AuthBlockingStub authBlockingStub;

    public TestTokenProvider(AuthGrpc.AuthBlockingStub authBlockingStub) {
        this.authBlockingStub = Assert.requireNonNull(authBlockingStub, "authBlockingStub");
    }

    @Override
    public String getToken(TokenType tokenType) throws SecurityException {
        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();
        builder.setTokenType(tokenType.getPTokenType());

        PSecurityResult result = null;
        try {
            PCmdGetTokenResponse response = authBlockingStub.getToken(builder.build());

            result = response.getResult();
            PAuthCode code = result.getCode();
            if (code == PAuthCode.OK) {
                String token = response.getAuthorizationToken().getToken();
                logger.debug("getToken() success. token:{}", token);
                return token;
            }
        } catch (Exception e) {
            throw new SecurityException(e.getMessage(), e);
        }

        throw new SecurityException(result.getMessage().getValue());
    }

}
