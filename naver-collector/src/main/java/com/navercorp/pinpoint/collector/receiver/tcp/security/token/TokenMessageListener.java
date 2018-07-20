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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
class TokenMessageListener extends ParallelMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationStateContext state = new AuthenticationStateContext();
    private final TokenSerDes tokenSerDes = new TokenSerDes();

    private final TokenService tokenService;

    private final TCPPacketHandlerFactory packetHandlerFactory;
    private final DispatchHandler dispatchHandler;

    private TCPPacketHandler packetHandler = null;

    TokenMessageListener(Executor executor, TokenService tokenService, TCPPacketHandlerFactory packetHandlerFactory, DispatchHandler dispatchHandler) {
        super(executor);
        this.tokenService = Assert.requireNonNull(tokenService, "tokenService must not be null");

        this.packetHandlerFactory = Assert.requireNonNull(packetHandlerFactory, "packetHandlerFactory must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

    @Override
    protected void handleSend0(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        if (state.isSucceeded()) {
            packetHandler.handleSend(sendPacket, pinpointSocket);
        } else {
            // abnormal situation : is unable to send data before authentication completed.
            logger.warn("send message will be discarded. cause:authentication is in progress. remote:{}, message:{}", pinpointSocket.getRemoteAddress(), sendPacket);
        }
    }

    @Override
    protected void handleRequest0(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        if (state.isSucceeded()) {
            packetHandler.handleRequest(requestPacket, pinpointSocket);
        } else {
            TCmdAuthenticationToken tokenRequest = getTokenRequest(requestPacket);
            if (tokenRequest != null) {
                if (state.changeStateProgress()) {
                    handleAuthentication(requestPacket, tokenRequest, pinpointSocket);
                } else {
                    // abnormal situation : is unable to send data before authentication completed.
                    logger.warn("request message will be discarded. cause:authentication is in progress. remote:{}, message:{}", pinpointSocket.getRemoteAddress(), requestPacket);
                }
            } else {
                // abnormal situation : is unable to send data before authentication completed.
                logger.warn("request message will be discarded. cause:authentication is in progress. remote:{}, message:{}", pinpointSocket.getRemoteAddress(), requestPacket);
            }
        }
    }

    private void handleAuthentication(RequestPacket requestPacket, TCmdAuthenticationToken request, PinpointSocket pinpointSocket) {
        try {
            logger.info("authentication start() started. remote:{}", pinpointSocket);

            final byte[] tokenPayload = request.getToken();
            final String tokenPayloadString = BytesUtils.toString(tokenPayload);
            final Token token = tokenService.getAndRemove(tokenPayloadString, TokenType.ALL);

            String remoteAddress = getRemoteAddress(pinpointSocket);
            if (token != null && token.getRemoteAddress().equals(remoteAddress)) {
                handleSuccess(pinpointSocket, requestPacket, token);
            } else {
                handleFail(pinpointSocket, requestPacket, TTokenResponseCode.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.warn("message:{}", e.getMessage(), e);
            handleFail(pinpointSocket, requestPacket, TTokenResponseCode.INTERNAL_SERVER_ERROR);
        }
        logger.info("authentication start() completed. remote:{}", pinpointSocket);
    }

    private TCmdAuthenticationToken getTokenRequest(RequestPacket requestPacket) {
        final byte[] payload = requestPacket.getPayload();
        return tokenSerDes.deserialize(payload, TCmdAuthenticationToken.class);
    }

    private String getRemoteAddress(PinpointSocket pinpointSocket) {
        SocketAddress remoteSocketAddress = pinpointSocket.getRemoteAddress();
        if (remoteSocketAddress instanceof InetSocketAddress) {
            return  ((InetSocketAddress) remoteSocketAddress).getAddress().getHostAddress();
        }
        return null;
    }

    private void handleSuccess(PinpointSocket pinpointSocket, RequestPacket requestPacket, Token token) {
        AttachTokenDispatchHandler attachTokenDispatchHandler = new AttachTokenDispatchHandler(token, this.dispatchHandler);
        this.packetHandler = packetHandlerFactory.build(attachTokenDispatchHandler);
        boolean change = state.changeStateSuccess();
        if (change) {
            response(pinpointSocket, requestPacket, TTokenResponseCode.OK);
        } else {
            handleFail(pinpointSocket, requestPacket, TTokenResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleFail(PinpointSocket pinpointSocket, RequestPacket requestPacket, TTokenResponseCode tokenResponseCode) {
        if (logger.isInfoEnabled()) {
            logger.info("authentication fail. cause:{}, remote:{}", tokenResponseCode.name(), pinpointSocket.getRemoteAddress());
        }

        try {
            state.changeStateFail();
            response(pinpointSocket, requestPacket, tokenResponseCode);
        } finally {
            if (pinpointSocket != null) {
                pinpointSocket.close();
            }
        }
    }

    private void response(PinpointSocket pinpointSocket, RequestPacket requestPacket, TTokenResponseCode tokenResponseCode) {
        TCmdAuthenticationTokenRes response = new TCmdAuthenticationTokenRes();
        response.setCode(tokenResponseCode);

        byte[] payload = tokenSerDes.serialize(response);
        if (payload == null) {
            logger.warn("TCmdAuthenticationTokenRes object serialization failed");
            return;
        }

        pinpointSocket.response(requestPacket.getRequestId(), payload);
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        if (state.isSucceeded()) {
            return HandshakeResponseCode.SIMPLEX_COMMUNICATION;
        } else {
            logger.warn("handshake message will be discarded. cause:authentication is not completed. properties:{}", properties);
            return null;
        }
    }

    @Override
    public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
    }

    boolean isAuthenticated() {
        return state.isSucceeded();
    }

}
