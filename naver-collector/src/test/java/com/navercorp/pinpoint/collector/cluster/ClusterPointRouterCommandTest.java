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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionFactory;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionRepository;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnector;
import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.collector.util.DefaultAddress;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterCommandTest {

    private final long currentTime = System.currentTimeMillis();

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @Autowired
    @Qualifier("commandHeaderTBaseSerializerFactory")
    private SerializerFactory commandSerializerFactory;

    @Autowired
    private DeserializerFactory commandDeserializerFactory;

    @After
    public void cleanup() {
        ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();
        List<TargetClusterPoint> clusterPointList = targetClusterPointRepository.getClusterPointList();
        for (TargetClusterPoint clusterPoint : clusterPointList) {
            targetClusterPointRepository.removeAndGetIsKeyRemoved(clusterPoint);
        }
    }

    @Test
    public void profilerClusterPointTest() throws TException, InterruptedException {
        String serverIdentifier = CollectorUtils.getServerIdentifier();

        CollectorClusterConnectionRepository clusterRepository = new CollectorClusterConnectionRepository();
        CollectorClusterConnectionFactory clusterConnectionFactory = new CollectorClusterConnectionFactory(serverIdentifier, clusterPointRouter, clusterPointRouter);
        CollectorClusterConnector clusterConnector = clusterConnectionFactory.createConnector();

        PinpointClientFactory agentFactory = null;

        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.NO_RESPONSE);

        TestPinpointServerAcceptor testCollectorAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        TestPinpointServerAcceptor testWebAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        CollectorClusterConnectionManager clusterManager = null;
        try {
            clusterManager = new CollectorClusterConnectionManager(serverIdentifier, clusterRepository, clusterConnector);
            clusterManager.start();

            agentFactory = createSocketFactory();
            int collectorPort = testCollectorAcceptor.bind();

            agentFactory.connect("127.0.0.1", collectorPort);
            
            Thread.sleep(100);
            
            List<PinpointSocket> writablePinpointServerList = testCollectorAcceptor.getConnectedPinpointSocketList();
            for (PinpointSocket writablePinpointServer : writablePinpointServerList) {
                ClusterPoint clusterPoint = new PinpointServerClusterPoint((DefaultPinpointServer)writablePinpointServer);
                
                ClusterPointRepository clusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();
                clusterPointRepository.addAndIsKeyCreated(clusterPoint);
            }

            int webPort = testWebAcceptor.bind();

            Address address = new DefaultAddress("127.0.0.1", webPort);
            clusterManager.connectPointIfAbsent(address);
            
  
            byte[] echoPayload = createEchoPayload("hello");
            byte[] commandDeliveryPayload = createDeliveryCommandPayload("application", "agent", currentTime, echoPayload);

            List<PinpointSocket> contextList = testWebAcceptor.getConnectedPinpointSocketList();
            PinpointSocket writablePinpointServer = contextList.get(0);
            Future<ResponseMessage> future = writablePinpointServer.request(commandDeliveryPayload);
            future.await();

            Message message1 = SerializationUtils.deserialize(future.getResult().getMessage(), commandDeserializerFactory);
            TCommandTransferResponse response = (TCommandTransferResponse) message1.getData();
            Message message2 = SerializationUtils.deserialize(response.getPayload(), commandDeserializerFactory);
            TCommandEcho echoResponse = (TCommandEcho) message2.getData();

            Assert.assertEquals(echoResponse.getMessage(), "hello");
        } finally {
            if (clusterManager != null) {
                clusterManager.stop();
            }
            
            if (agentFactory  != null) {
                agentFactory.release();
            }

            testCollectorAcceptor.close();
            testWebAcceptor.close();
        }
    }

    private byte[] createEchoPayload(String message) throws TException {
        TCommandEcho echo = new TCommandEcho();
        echo.setMessage("hello");

        byte[] payload = SerializationUtils.serialize(echo, commandSerializerFactory);
        return payload;
    }

    private byte[] createDeliveryCommandPayload(String application, String agent, long currentTime, byte[] echoPayload) throws TException {
        TCommandTransfer commandTransfer = new TCommandTransfer();
        commandTransfer.setApplicationName("application");
        commandTransfer.setAgentId("agent");
        commandTransfer.setStartTime(currentTime);
        commandTransfer.setPayload(echoPayload);

        byte[] payload = SerializationUtils.serialize(commandTransfer, commandSerializerFactory);
        return payload;
    }

    private Map<String, Object> getParams() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(HandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(HandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(HandshakePropertyType.IP.getName(), "ip");
        properties.put(HandshakePropertyType.PID.getName(), 1111);
        properties.put(HandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), currentTime);
        properties.put(HandshakePropertyType.VERSION.getName(), "1.0.3-SNAPSHOT");

        return properties;
    }

    private PinpointClientFactory createSocketFactory() {
        PinpointClientFactory factory = new DefaultPinpointClientFactory();
        factory.setProperties(getParams());
        factory.setMessageListener(TestServerMessageListenerFactory.create(TestServerMessageListenerFactory.HandshakeType.SIMPLEX, TestServerMessageListenerFactory.ResponseType.ECHO));
        
        return factory;
    }

}
