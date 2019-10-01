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
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityMetadata;

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
public class AuthenticationKeyInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String authenticationKey;

    public AuthenticationKeyInterceptor(String authenticationKey) {
        this.authenticationKey = Assert.requireNonNull(authenticationKey, "authenticationKey must not be null");
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        if (logger.isDebugEnabled()) {
            logger.debug("interceptCall {}", method.getFullMethodName());
        }

        final ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
        final ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> forwardingClientCall = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(clientCall) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                GrpcSecurityMetadata.setAuthKey(headers, authenticationKey);
                super.start(responseListener, headers);
            }

        };
        return forwardingClientCall;
    }

}


