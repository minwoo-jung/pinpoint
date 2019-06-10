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

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPServerResponse;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.BasicPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.security.SecurityConstants;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taejin Koo
 */
public class TokenEnableTCPPacketHandler implements TCPPacketHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DispatchHandler dispatchHandler;

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    private final NamespaceService namespaceService;

    private final ConcurrentMap<String, NameSpaceInfo> namespaceMap = new ConcurrentHashMap<>();

    public TokenEnableTCPPacketHandler(DispatchHandler dispatchHandler, SerializerFactory<HeaderTBaseSerializer> serializerFactory, DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory, NamespaceService namespaceService) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.serializerFactory = Objects.requireNonNull(serializerFactory, "serializerFactory must not be null");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory must not be null");
        this.namespaceService = Assert.requireNonNull(namespaceService, "namespaceService must not be null");
    }

    @Override
    public void handleSend(SendPacket packet, PinpointSocket pinpointSocket) {
        Objects.requireNonNull(packet, "packet must not be null");
        Objects.requireNonNull(pinpointSocket, "pinpointSocket must not be null");

        final byte[] payload = getPayload(packet);
        final InetSocketAddress remoteAddress = (InetSocketAddress) pinpointSocket.getRemoteAddress();
        try {
            Message<TBase<?, ?>> message = SerializationUtils.deserialize(payload, deserializerFactory);
            ServerRequest<TBase<?, ?>> serverRequest = newServerRequest(message, remoteAddress);
            dispatchHandler.dispatchSendMessage(serverRequest);
        } catch (TException e) {
            handleTException(payload, remoteAddress, e);
        } catch (Exception e) {
            // there are cases where invalid headers are received
            handleException(payload, remoteAddress, e);
        }
    }

    private ServerRequest<TBase<?, ?>> newServerRequest(Message<TBase<?, ?>> message, InetSocketAddress remoteSocketAddress) {
        final String remoteAddress = remoteSocketAddress.getAddress().getHostAddress();
        final int remotePort = remoteSocketAddress.getPort();

        DefaultServerRequest<TBase<?, ?>> serverRequest = new DefaultServerRequest<>(message, remoteAddress, remotePort);
        setNamespaceInfo(serverRequest, getNameSpaceInfo(message.getHeaderEntity()));

        return serverRequest;
    }

    public byte[] getPayload(BasicPacket packet) {
        final byte[] payload = packet.getPayload();
        Objects.requireNonNull(payload, "payload must not be null");
        return payload;
    }

    @Override
    public void handleRequest(RequestPacket packet, PinpointSocket pinpointSocket) {
        Objects.requireNonNull(packet, "packet must not be null");
        Objects.requireNonNull(pinpointSocket, "pinpointSocket must not be null");

        final byte[] payload = getPayload(packet);
        final InetSocketAddress remoteAddress = (InetSocketAddress) pinpointSocket.getRemoteAddress();

        try {
            Message<TBase<?, ?>> message = SerializationUtils.deserialize(payload, deserializerFactory);

            ServerRequest<TBase<?, ?>> request = newServerRequest(message, remoteAddress);
            ServerResponse<TBase<?, ?>> response = new TCPServerResponse(serializerFactory, pinpointSocket, packet.getRequestId());
            dispatchHandler.dispatchRequestMessage(request, response);
        } catch (TException e) {
            handleTException(payload, remoteAddress, e);
        } catch (Exception e) {
            handleException(payload, remoteAddress, e);
        }
    }

    private void handleTException(byte[] payload, SocketAddress remoteAddress, TException e) {
        if (logger.isWarnEnabled()) {
            logger.warn("packet deserialize error. remote:{} cause:{}", remoteAddress, e.getMessage(), e);
        }
        if (isDebug) {
            logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(payload));
        }
        throw new RuntimeException("serialized fail ", e);
    }

    private void handleException(byte[] payload, SocketAddress remoteAddress, Exception e) {
        // there are cases where invalid headers are received
        if (logger.isWarnEnabled()) {
            logger.warn("Unexpected error. remote:{} cause:{}", remoteAddress, e.getMessage(), e);
        }
        if (isDebug) {
            logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(payload));
        }
    }


    private void setNamespaceInfo(ServerRequest serverRequest, NameSpaceInfo nameSpaceInfo) {
        serverRequest.setAttribute(NameSpaceInfo.NAMESPACE_INFO, nameSpaceInfo);
    }

    private NameSpaceInfo getNameSpaceInfo(HeaderEntity headerEntity) {
        if (headerEntity == null) {
            return null;
        }
        final String licenseKey = headerEntity.getEntity(SecurityConstants.KEY_LICENSE_KEY);
        if (!StringUtils.hasText(licenseKey)) {
            return null;
        }

        final NameSpaceInfo nameSpaceInfo = namespaceMap.get(licenseKey);
        if (nameSpaceInfo != null) {
            return nameSpaceInfo;
        }

        final NameSpaceInfo newNameSpaceInfo = getNameSpaceInfoFromDao(licenseKey);
        if (newNameSpaceInfo != null) {
            namespaceMap.putIfAbsent(licenseKey, newNameSpaceInfo);
        }

        return newNameSpaceInfo;
    }

    private NameSpaceInfo getNameSpaceInfoFromDao(String licenseKey) {
        final PaaSOrganizationKey organizationKey = namespaceService.selectPaaSOrganizationkey(licenseKey);
        if (organizationKey == null) {
            return null;
        }

        final PaaSOrganizationInfo paaSOrganizationInfo = namespaceService.selectPaaSOrganizationInfo(organizationKey.getOrganization());
        if (paaSOrganizationInfo == null) {
            return null;
        }

        final String organization = paaSOrganizationInfo.getOrganization();
        final String databaseName = paaSOrganizationInfo.getDatabaseName();
        final String hbaseNameSpace = paaSOrganizationInfo.getHbaseNameSpace();

        if (StringUtils.hasText(organization) && StringUtils.hasText(databaseName) && StringUtils.hasText(hbaseNameSpace)) {
            return new NameSpaceInfo(organization, databaseName, hbaseNameSpace);
        }
        return null;
    }

}
