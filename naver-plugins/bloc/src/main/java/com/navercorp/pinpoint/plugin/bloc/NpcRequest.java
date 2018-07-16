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

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.nhncorp.lucy.npc.NpcMessage;
import external.org.apache.mina.common.IoSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NpcRequest implements ServerRequestWrapper {

    private static final String NAMESPACE_URA = "URA 1.0";
    private static final String UNKNOWN_ADDRESS = "Unknown Address";

    private final IoSession ioSession;
    private final NpcMessage npcMessage;
    private Map<String, String> pinpointOptions;

    public NpcRequest(IoSession ioSession, NpcMessage npcMessage) {
        this.ioSession = ioSession;
        this.npcMessage = npcMessage;
    }

    public IoSession getIoSession() {
        return ioSession;
    }

    public NpcMessage getNpcMessage() {
        return npcMessage;
    }

    @Override
    public String getRpcName() {
        final String rpcName = LucyNetUtils.getRpcName(npcMessage);
        return rpcName;
    }

    @Override
    public String getEndPoint() {
        final String endPoint = getLocalAddress(ioSession);
        return endPoint;
    }

    @Override
    public String getRemoteAddress() {
        final String remoteAddress = getRemoteAddress(ioSession);
        return remoteAddress;
    }

    @Override
    public String getAcceptorHost() {
        return getEndPoint();
    }

    @Override
    public String getHeader(String name) {
        if (pinpointOptions == null) {
            this.pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);
        }
        return pinpointOptions.get(name);
    }

    private String getRemoteAddress(IoSession ioSession) {
        if (ioSession == null) {
            return UNKNOWN_ADDRESS;
        }

        return getIpPort(ioSession.getRemoteAddress());
    }

    private String getLocalAddress(IoSession ioSession) {
        if (ioSession == null) {
            return UNKNOWN_ADDRESS;
        }

        return getIpPort(ioSession.getLocalAddress());
    }

    private String getIpPort(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return UNKNOWN_ADDRESS;
        }

        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) socketAddress;
            return HostAndPort.toHostAndPortString(addr.getAddress().getHostAddress(), addr.getPort());
        }

        String address = socketAddress.toString();
        int addressLength = address.length();

        if (addressLength > 0) {
            if (address.startsWith("/")) {
                return address.substring(1);
            } else {
                int delimiterIndex = address.indexOf('/');
                if (delimiterIndex != -1 && delimiterIndex < addressLength) {
                    return address.substring(address.indexOf('/') + 1);
                }
            }
        }

        return address;
    }
}
