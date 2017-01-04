/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.collector.sender;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class TcpDataSenderFactory implements FactoryBean {

    private final String flinkServerIp;
    private final int flinkServerPort;

    public TcpDataSenderFactory(String flinkServerIp, int flinkServerPort) {
        this.flinkServerIp = flinkServerIp;
        this.flinkServerPort = flinkServerPort;
    }

    public Object getObject() throws Exception {
        CommandDispatcher commandDispatcher = new CommandDispatcher();
        return createTcpDataSender(commandDispatcher);
    }

    @Override
    public Class<?> getObjectType() {
        return TcpDataSender.class;
    }

    public boolean isSingleton() {
        return true;
    }

    private TcpDataSender createTcpDataSender(CommandDispatcher commandDispatcher) {
        PinpointClientFactory clientFactory = createPinpointClientFactory(commandDispatcher);
        PinpointClient client = ClientFactoryUtils.createPinpointClient(flinkServerIp, flinkServerPort, clientFactory);
        return new TcpDataSender(client);
    }

    private PinpointClientFactory createPinpointClientFactory(CommandDispatcher commandDispatcher) {
        PinpointClientFactory pinpointClientFactory = new PinpointClientFactory();
        pinpointClientFactory.setTimeoutMillis(1000 * 5);

//        Map<String, Object> properties = this.agentInformation.toMap();

//        boolean isSupportServerMode = this.profilerConfig.isTcpDataSenderCommandAcceptEnable();
//
//        if (isSupportServerMode) {
//            pinpointClientFactory.setMessageListener(commandDispatcher);
//            pinpointClientFactory.setServerStreamChannelMessageListener(commandDispatcher);
//
//            properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), true);
//        } else {
//            properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), false);
//        }
        Map<String, Object> properties = new HashMap<>();
        pinpointClientFactory.setProperties(properties);
        return pinpointClientFactory;
    }
}
