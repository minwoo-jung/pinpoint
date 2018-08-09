/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.tcp.DefaultTCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationToken;
import org.apache.thrift.TBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.SocketUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class TokenMessageListenerTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(50, 1000);

    private final TokenSerDes tokenSerDes = new TokenSerDes();
    private final AtomicInteger requestCount = new AtomicInteger();

    private ExecutorService executorService;
    private TokenService tokenService;


    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(1);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + 3000;

        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("org", "namespace", "hbaseNamespace");

        Token token = new Token("key", paaSOrganizationInfo, endTime, "127.0.0.1", TokenType.SPAN);

        TokenService tokenService = Mockito.mock(TokenService.class);
        Mockito.when(tokenService.getAndRemove("success", TokenType.ALL)).thenReturn(token);

        this.tokenService = tokenService;
    }

    @After
    public void tearDown() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
        }
    }


    @Test
    public void authenticateTest1() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();
        TokenMessageListener tokenMessageListener = new TokenMessageListener(executorService, tokenService, new DefaultTCPPacketHandlerFactory(), countingDispatchHandler);

        checkMessageReceived(countingDispatchHandler, tokenMessageListener, false);
        authenticate(tokenMessageListener, true);
        checkMessageReceived(countingDispatchHandler, tokenMessageListener, true);
    }

    @Test
    public void authenticateTest2() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();
        TokenMessageListener tokenMessageListener = new TokenMessageListener(executorService, tokenService, new DefaultTCPPacketHandlerFactory(), countingDispatchHandler);

        checkMessageReceived(countingDispatchHandler, tokenMessageListener, false);

        authenticate(tokenMessageListener, false);

        checkMessageReceived(countingDispatchHandler, tokenMessageListener, false);
    }


    private SendPacket createSendPacket(TBase tBase) {
        byte[] payload = tokenSerDes.serialize(tBase);
        return new SendPacket(payload);
    }

    private RequestPacket createRequestPacket(TBase tBase) {
        byte[] payload = tokenSerDes.serialize(tBase);
        return new RequestPacket(requestCount.incrementAndGet(), payload);
    }

    private TCmdAuthenticationToken createTokenRequest(byte[] payload) {
        TCmdAuthenticationToken tokenRequest = new TCmdAuthenticationToken();
        tokenRequest.setToken(payload);

        return tokenRequest;
    }

    private void authenticate(TokenMessageListener tokenMessageListener, boolean success) {
        PinpointSocket pinpointSocket = Mockito.mock(PinpointSocket.class);
        int availablePort = SocketUtils.findAvailableTcpPort(22214);
        Mockito.when(pinpointSocket.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", availablePort));

        byte[] payload = null;
        if (success) {
            payload = "success".getBytes();
        } else {
            payload = "fail".getBytes();
        }

        TCmdAuthenticationToken tokenRequest = createTokenRequest(payload);
        RequestPacket requestPacket = createRequestPacket(tokenRequest);
        tokenMessageListener.handleRequest(requestPacket, pinpointSocket);

        boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return tokenMessageListener.isAuthenticated();
            }
        });

        if (success) {
            Assert.assertTrue(await);
        } else {
            Assert.assertFalse(await);
        }
    }

    private void checkMessageReceived(CountingDispatchHandler countingDispatchHandler, TokenMessageListener tokenMessageListener, boolean success) {
        PinpointSocket pinpointSocket = Mockito.mock(PinpointSocket.class);
        int availablePort = SocketUtils.findAvailableTcpPort(22214);
        Mockito.when(pinpointSocket.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", availablePort));

        tokenMessageListener.handleSend(createSendPacket(new TResult()), pinpointSocket);
        tokenMessageListener.handleSend(createSendPacket(new TResult()), pinpointSocket);
        tokenMessageListener.handleRequest(createRequestPacket(new TResult()), pinpointSocket);

        boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {

                if (!(countingDispatchHandler.getCalledSendServerRequestCount() == 2)) {
                    return false;
                }
                if (!(countingDispatchHandler.getCalledRequestServerRequestCount() == 1)) {
                    return false;
                }
                return true;
            }
        });

        if (success) {
            Assert.assertTrue(await);
        } else {
            Assert.assertFalse(await);
        }
    }

}
