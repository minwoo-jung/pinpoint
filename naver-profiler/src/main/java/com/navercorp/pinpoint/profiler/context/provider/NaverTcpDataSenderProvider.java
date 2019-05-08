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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ThriftTransportConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.NaverProfilerConfigConstants;
import com.navercorp.pinpoint.profiler.context.module.DefaultClientFactory;
import com.navercorp.pinpoint.profiler.context.thrift.BypassMessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftMessageSerializer;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.security.SecurityConstants;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.TBaseSerializer;
import org.apache.thrift.TBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class NaverTcpDataSenderProvider implements Provider<EnhancedDataSender> {

    private final ProfilerConfig profilerConfig;
    private final Provider<PinpointClientFactory> clientFactoryProvider;
    private final Provider<HeaderTBaseSerializer> tBaseSerializerProvider;
    private final Provider<AgentInformation> agentInformation;
    private final CommandDispatcher commandDispatcher;

    @Inject
    public NaverTcpDataSenderProvider(ProfilerConfig profilerConfig, @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider, Provider<HeaderTBaseSerializer> tBaseSerializerProvider, Provider<AgentInformation> agentInformation, CommandDispatcher commandDispatcher) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.clientFactoryProvider = Assert.requireNonNull(clientFactoryProvider, "clientFactoryProvider must not be null");
        this.tBaseSerializerProvider = Assert.requireNonNull(tBaseSerializerProvider, "tBaseSerializerProvider must not be null");
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation must not be null");
        this.commandDispatcher = Assert.requireNonNull(commandDispatcher, "commandDispatcher must not be null");
    }

    @Override
    public EnhancedDataSender get() {
        PinpointClientFactory clientFactory = clientFactoryProvider.get();

        String licenseKey = profilerConfig.readString(NaverProfilerConfigConstants.KEY_LICENSE_KEY, null);
        Assert.requireNonNull(licenseKey, "licenseKey must not be null");

        Map<String, Object> properties = getProperties(licenseKey);
        clientFactory.setProperties(properties);

        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        String collectorTcpServerIp = thriftTransportConfig.getCollectorTcpServerIp();
        int collectorTcpServerPort = thriftTransportConfig.getCollectorTcpServerPort();
        TBaseSerializer tBaseSerializer = tBaseSerializerProvider.get();

        TBaseSerializer licenseSupportSerializer = new LicenseSupportTBaseSerializer(tBaseSerializer, licenseKey);

        MessageConverter<TBase<?, ?>> messageConverter = new BypassMessageConverter<TBase<?, ?>>();
        MessageSerializer<byte[]> messageSerializer = new ThriftMessageSerializer(messageConverter, licenseSupportSerializer);


        return new TcpDataSender("Naver-Default", collectorTcpServerIp, collectorTcpServerPort, clientFactory, messageSerializer);
    }

    // com.navercorp.pinpoint.profiler.context.provider.thrift.PinpointClientFactoryProvider 의 Handshake 데이터를 포함하는 것과 같은 로직입니다.
    // 이후에 Handshake 데이터가 추가되면 이곳에도 함께 추가되어야 합니다.
    private Map<String, Object> getProperties(String licenseKey) {
        AgentInformation agentInformation = this.agentInformation.get();
        Map<String, Object> properties = toMap(agentInformation);

        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        boolean isSupportServerMode = thriftTransportConfig.isTcpDataSenderCommandAcceptEnable();

        if (isSupportServerMode) {
            properties.put(HandshakePropertyType.SUPPORT_SERVER.getName(), true);
            properties.put(HandshakePropertyType.SUPPORT_COMMAND_LIST.getName(), commandDispatcher.getRegisteredCommandServiceCodes());
        } else {
            properties.put(HandshakePropertyType.SUPPORT_SERVER.getName(), false);
        }

        properties.put(SecurityConstants.KEY_LICENSE_KEY, licenseKey);

        return properties;
    }

    private Map<String, Object> toMap(AgentInformation agentInformation) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(HandshakePropertyType.AGENT_ID.getName(), agentInformation.getAgentId());
        map.put(HandshakePropertyType.APPLICATION_NAME.getName(), agentInformation.getApplicationName());
        map.put(HandshakePropertyType.HOSTNAME.getName(), agentInformation.getMachineName());
        map.put(HandshakePropertyType.IP.getName(), agentInformation.getHostIp());
        map.put(HandshakePropertyType.PID.getName(), agentInformation.getPid());
        map.put(HandshakePropertyType.SERVICE_TYPE.getName(), agentInformation.getServerType().getCode());
        map.put(HandshakePropertyType.START_TIMESTAMP.getName(), agentInformation.getStartTime());
        map.put(HandshakePropertyType.VERSION.getName(), agentInformation.getAgentVersion());

        return map;
    }

}