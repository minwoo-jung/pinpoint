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

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;

import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
class TokenMessageListenerFactory implements ServerMessageListenerFactory<TokenMessageListener> {

    private final Executor executor;
    private final TokenService tokenService;

    private final TCPPacketHandlerFactory packetHandlerFactory;
    private final DispatchHandler dispatchHandler;

    TokenMessageListenerFactory(Executor executor, TokenService tokenService, TCPPacketHandlerFactory packetHandlerFactory, DispatchHandler dispatchHandler) {
        this.executor = Assert.requireNonNull(executor, "executor must not be null");
        this.tokenService = Assert.requireNonNull(tokenService, "tokenService must not be null");

        this.packetHandlerFactory = Assert.requireNonNull(packetHandlerFactory, "packetHandlerFactory must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

    @Override
    public TokenMessageListener create() {
        return new TokenMessageListener(executor, tokenService, packetHandlerFactory, dispatchHandler);
    }

}
