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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.PinpointServerAcceptorProvider;
import com.navercorp.pinpoint.collector.receiver.thrift.TCPReceiverBean;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.DefaultTCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPReceiver;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
public class TokenTCPReceiverBean extends TCPReceiverBean {

    private TokenService tokenService;

    @Override
    protected TCPReceiver createTcpReceiver(String beanName, String bindIp, int port, Executor executor, DispatchHandler dispatchHandler, TCPPacketHandlerFactory tcpPacketHandlerFactory, PinpointServerAcceptorProvider acceptorProvider) {
        Assert.requireNonNull(tokenService, "tokenService must not be null");

        InetSocketAddress bindAddress = new InetSocketAddress(bindIp, port);

        if (tcpPacketHandlerFactory != null) {
            ServerMessageListenerFactory messageListenerFactory = new TokenMessageListenerFactory(executor, tokenService, tcpPacketHandlerFactory, dispatchHandler);
            return new TokenTCPReceiver(beanName, bindAddress, acceptorProvider, messageListenerFactory);
        } else {
            ServerMessageListenerFactory messageListenerFactory = new TokenMessageListenerFactory(executor, tokenService, new DefaultTCPPacketHandlerFactory(), dispatchHandler);
            return new TokenTCPReceiver(beanName, bindAddress, acceptorProvider, messageListenerFactory);
        }
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

}
