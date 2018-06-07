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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class UnsupportedServerMessageListenerFactory implements ServerMessageListenerFactory  {

    @Override
    public ServerMessageListener create() {
        return new UnsupportedServerMessageListener();
    }


    private static class UnsupportedServerMessageListener implements ServerMessageListener {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.warn("Unsupport send received {} {}", sendPacket, pinpointSocket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.warn("Unsupport request received {} {}", requestPacket, pinpointSocket);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.warn("do Handshake {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            logger.warn("Unsupported ping received packet:{}, remote:{}", pingPacket, pinpointServer);
        }

    }

}
