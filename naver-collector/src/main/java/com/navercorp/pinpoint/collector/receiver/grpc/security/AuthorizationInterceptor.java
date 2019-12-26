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

package com.navercorp.pinpoint.collector.receiver.grpc.security;

import com.navercorp.pinpoint.collector.receiver.grpc.security.service.AuthTokenService;
import com.navercorp.pinpoint.grpc.security.TokenType;
import com.navercorp.pinpoint.grpc.security.server.AuthContext;
import com.navercorp.pinpoint.grpc.security.server.AuthState;
import com.navercorp.pinpoint.grpc.security.server.DefaultAuthStateContext;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityAttribute;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityContext;
import com.navercorp.pinpoint.grpc.security.GrpcSecurityMetadata;
import com.navercorp.pinpoint.grpc.server.ServerContext;

import io.grpc.Attributes;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

/**
 * @author Taejin Koo
 */
@Profile("tokenAuthentication")
public class AuthorizationInterceptor implements ServerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthTokenService authTokenService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, Metadata headers, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        if (GrpcSecurityContext.getAuthContext() != null) {
            return processCall(serverCall, headers, serverCallHandler);
        }

        String authToken = GrpcSecurityMetadata.getAuthToken(headers);
        if (logger.isDebugEnabled()) {
            logger.debug("Received token({}) from remote agent.", authToken);
        }

        synchronized (this) {
            if (GrpcSecurityContext.getAuthContext() != null) {
                return processCall(serverCall, headers, serverCallHandler);
            }

            // FIXME SPAN으로 지정해 두었는데 SPAN, STAT 을 파라미터로 넣게 해야함
            final DefaultAuthStateContext defaultAuthStateContext = new DefaultAuthStateContext();
            boolean authorization = authTokenService.authorization(authToken, TokenType.SPAN);
            if (authorization) {
                defaultAuthStateContext.changeState(AuthState.SUCCESS);
                Context newContext = GrpcSecurityContext.setAuthTokenHolder(authToken);
                ServerCall.Listener<ReqT> contextPropagateInterceptor = Contexts.interceptCall(newContext, serverCall, headers, serverCallHandler);
                return contextPropagateInterceptor;
            } else {
                defaultAuthStateContext.changeState(AuthState.FAIL);
                SecurityException securityException = new SecurityException("authorization failed");
                serverCall.close(Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException), new Metadata());
                return disabledCall();
            }
        }
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> processCall(ServerCall<ReqT, RespT> serverCall, Metadata headers, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        AuthContext authContext = GrpcSecurityContext.getAuthContext();
        AuthState authState = authContext.getState();

        logger.warn("authState:{}", authState);
        if (authState == AuthState.SUCCESS) {
            String authToken = GrpcSecurityMetadata.getAuthToken(headers);
            Context newContext = GrpcSecurityContext.setAuthTokenHolder(authToken);
            ServerCall.Listener<ReqT> contextPropagateInterceptor = Contexts.interceptCall(newContext, serverCall, headers, serverCallHandler);
            return contextPropagateInterceptor;
        } else if (authState == AuthState.FAIL) {
            SecurityException securityException = new SecurityException("authorization failed");
            serverCall.close(Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException), new Metadata());
            return disabledCall();
        } else if (authState == AuthState.EXPIRED) {
            serverCall.close(Status.UNAUTHENTICATED.withDescription("already expired"), new Metadata());
            return disabledCall();
        } else {
            SecurityException securityException = new SecurityException("unknown state");
            serverCall.close(Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException), new Metadata());
            return disabledCall();
        }
    }

    private <ReqT> ServerCall.Listener<ReqT> disabledCall() {
        return new ServerCall.Listener<ReqT>() {
        };
    }

}
