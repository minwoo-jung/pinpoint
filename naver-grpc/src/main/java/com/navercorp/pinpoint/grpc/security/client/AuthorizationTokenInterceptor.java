/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.security.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.security.GrpcSecurityMetadata;
import com.navercorp.pinpoint.grpc.security.TokenType;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class AuthorizationTokenInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TokenType tokenType;
    private final AuthorizationTokenProvider authorizationTokenProvider;

    public AuthorizationTokenInterceptor(TokenType tokenType, AuthorizationTokenProvider authorizationTokenProvider) {
        this.tokenType = Assert.requireNonNull(tokenType, "tokenType must not be null");
        this.authorizationTokenProvider = Assert.requireNonNull(authorizationTokenProvider, "authorizationTokenProvider");
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        // TODO & FIXME for temp SPAN
        try {
            final String token = authorizationTokenProvider.getToken(tokenType);
            // TokenProvider
            final ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
            final ClientCall<ReqT, RespT> forwardingClientCall = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(clientCall) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    GrpcSecurityMetadata.setAuthToken(headers, token);
                    super.start(responseListener, headers);
                }

            };
            return forwardingClientCall;
        } catch (Exception e) {
            logger.warn("Failed to get token. message:{}", e.getMessage(), e);
        }

        return next.newCall(method, callOptions);
    }

}
