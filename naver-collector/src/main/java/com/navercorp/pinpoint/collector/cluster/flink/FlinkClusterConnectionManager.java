/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class FlinkClusterConnectionManager implements ClusterConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DefaultPinpointClientFactory pinpointClientFactory;
    private final TcpDataSenderRepository tcpDataSenderRepository;


    public FlinkClusterConnectionManager(TcpDataSenderRepository tcpDataSenderRepository) {
        this.tcpDataSenderRepository = tcpDataSenderRepository;
        this.pinpointClientFactory = new DefaultPinpointClientFactory();
        this.pinpointClientFactory.setTimeoutMillis(1000 * 5);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        for (TcpDataSender tcpDataSender : tcpDataSenderRepository.getClusterSocketList()) {
            tcpDataSender.stop();
        }
        logger.info("{} stop completed.", this.getClass().getSimpleName());
    }

    @Override
    public void connectPointIfAbsent(InetSocketAddress address) {
        logger.info("localhost -> {} connect started.", address);

        if (tcpDataSenderRepository.containsKey(address)) {
            logger.info("localhost -> {} already connected.", address);
            return;
        }

        tcpDataSenderRepository.putIfAbsent(address, createTcpDataSender(address));

        logger.info("localhost -> {} connect completed.", address);
    }

    @Override
    public void disconnectPoint(SocketAddress address) {
        logger.info("localhost -> {} disconnect started.", address);

        TcpDataSender tcpDataSender = tcpDataSenderRepository.remove(address);
        if (tcpDataSender != null) {
            tcpDataSender.stop();
            logger.info("localhost -> {} disconnect completed.", address);
        } else {
            logger.info("localhost -> {} already disconnected.", address);
        }
    }

    @Override
    public List<SocketAddress> getConnectedAddressList() {
        return tcpDataSenderRepository.getAddressList();
    }

    private TcpDataSender createTcpDataSender(InetSocketAddress address) {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        CommandDispatcher commandDispatcher = new CommandDispatcher(builder.build());
        PinpointClient client = ClientFactoryUtils.createPinpointClient(address, pinpointClientFactory);
        return new TcpDataSender(client);
    }
}
