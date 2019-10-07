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
import com.navercorp.pinpoint.grpc.security.server.AuthContext;
import com.navercorp.pinpoint.grpc.security.server.AuthState;
import com.navercorp.pinpoint.grpc.security.server.DefaultAuthStateContext;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityAttribute;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityContext;
import com.navercorp.pinpoint.grpc.security.GrpcSecurityMetadata;

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
public class AuthenticationInterceptor implements ServerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthTokenService authTokenService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, Metadata headers, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final Attributes attributes = serverCall.getAttributes();
        final AuthContext authContext = GrpcSecurityAttribute.getAuthContext(attributes);

        AuthState authState = authContext.getState();
        if (authState == AuthState.SUCCESS) {
            String authKey = GrpcSecurityMetadata.getAuthKey(headers);
            Context newContext = GrpcSecurityContext.setAuthKeyHolder(authKey);
            ServerCall.Listener<ReqT> contextPropagateInterceptor = Contexts.interceptCall(newContext, serverCall, headers, serverCallHandler);
            return contextPropagateInterceptor;
        } else if (authState == AuthState.FAIL) {
            SecurityException securityException = new SecurityException("authentication failed");
            throw Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException).asRuntimeException();
        } else if (authState == AuthState.EXPIRED) {
            SecurityException securityException = new SecurityException("authentication expired");
            throw Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException).asRuntimeException();
        } else if (authState == AuthState.NONE) {
            String authKey = GrpcSecurityMetadata.getAuthKey(headers);

            logger.debug("authKey:{}", authKey);

            boolean authenticate = authTokenService.authenticate(authKey);
            if (authenticate) {
                if (authContext instanceof DefaultAuthStateContext) {
                    boolean changed = ((DefaultAuthStateContext) authContext).changeState(AuthState.SUCCESS);
                    Context newContext = GrpcSecurityContext.setAuthKeyHolder(authKey);
                    ServerCall.Listener<ReqT> contextPropagateInterceptor = Contexts.interceptCall(newContext, serverCall, headers, serverCallHandler);
                    return contextPropagateInterceptor;
                } else {
                    throw new SecurityException("InternalException : can not cast type to DefaultAuthStateContext");
                }
            } else {
                if (authContext instanceof DefaultAuthStateContext) {
                    ((DefaultAuthStateContext) authContext).changeState(AuthState.FAIL);
                    SecurityException securityException = new SecurityException("authentication failed");
                    throw Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException).asRuntimeException();
                } else {
                    throw new SecurityException("InternalException : can not cast type to DefaultAuthStateContext");
                }
            }
        } else {
            SecurityException securityException = new SecurityException("unknown state");
            throw Status.UNAUTHENTICATED.withDescription(securityException.getMessage()).withCause(securityException).asRuntimeException();
        }
    }

}

