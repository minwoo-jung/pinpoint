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

package com.navercorp.pinpoint.profiler.sender.sender.security;

import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PAuthCode;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PSecurityResult;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.security.TokenType;
import com.navercorp.pinpoint.grpc.security.client.AuthorizationTokenProvider;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.grpc.AgentGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;

import com.google.protobuf.GeneratedMessageV3;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Taejin Koo
 */
public class SecurityAgentGrpcDataSender extends AgentGrpcDataSender implements AuthorizationTokenProvider {

    private final AuthGrpc.AuthBlockingStub authBlockingStub;

    public SecurityAgentGrpcDataSender(String host, int port, int executorQueueSize,
                                       MessageConverter<GeneratedMessageV3> messageConverter, ReconnectExecutor reconnectExecutor, ScheduledExecutorService retransmissionExecutor,
                                       ChannelFactory channelFactory, ActiveTraceRepository activeTraceRepository) {
        super(host, port, executorQueueSize, messageConverter, reconnectExecutor, retransmissionExecutor, channelFactory, activeTraceRepository);

        authBlockingStub = createAuthStub();
    }

    private AuthGrpc.AuthBlockingStub createAuthStub() {
        return AuthGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public String getToken(TokenType tokenType) throws SecurityException {
        PCmdGetTokenRequest tokenRequest = createTokenRequest(tokenType);
        PCmdGetTokenResponse response = authBlockingStub.getToken(tokenRequest);

        PSecurityResult result = response.getResult();
        if (result.getCode() == PAuthCode.OK) {
            return response.getAuthorizationToken().getToken();
        }

        throw new SecurityException(result.getMessage().getValue());
    }

    private PCmdGetTokenRequest createTokenRequest(TokenType tokenType) {
        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();
        builder.setTokenType(tokenType.getPTokenType());
        return builder.build();
    }

}
