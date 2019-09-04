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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterPointStateChangedEventHandler;
import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;

import org.apache.curator.test.TestingServer;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperProfilerClusterServiceTest {

    private static final int DEFAULT_ACCEPTOR_PORT = SocketUtils.findAvailableTcpPort(22213);

    private static Timer testTimer;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @BeforeClass
    public static void setUp() {
        testTimer = TimerFactory.createHashedWheelTimer(ZookeeperProfilerClusterServiceTest.class.getSimpleName(), 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        testTimer.stop();
    }

    @Test
    public void simpleTest1() throws Exception {
        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
    @Test
    public void simpleTest2() throws Exception {
        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            service = ClusterTestUtils.createZookeeperClusterService("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            Thread.sleep(1000);

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 10000);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
    // 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
    @Test
    public void simpleTest3() throws Exception {
        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            ts.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);

            ts.restart();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 2000);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
    @Test
    public void simpleTest4() throws Exception {
        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            service = ClusterTestUtils.createZookeeperClusterService("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            Thread.sleep(1000);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            pinpointServer.stop();
            Thread.sleep(1000);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
            Thread.sleep(1000);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
        }
    }

    //
    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
    // 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
    @Test
    public void simpleTest5() throws Exception {
        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);

            ts.stop();
            Thread.sleep(1000);
            ts.restart();
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
        }
    }

    private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
        try {
            mockZookeeperServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stateToDuplex(PinpointServer pinpointServer) throws ProtocolException {
        byte[] payload = ControlMessageEncodingUtils.encode(getParams());
        ControlHandshakePacket handshakePacket = new ControlHandshakePacket(0, payload);
        pinpointServer.messageReceived(handshakePacket);
    }

    private Map<String, Object> getParams() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(HandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(HandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(HandshakePropertyType.IP.getName(), "ip");
        properties.put(HandshakePropertyType.PID.getName(), 1111);
        properties.put(HandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(HandshakePropertyType.VERSION.getName(), "1.0");

        return properties;
    }

    private PinpointServerConfig createPinpointServerConfig(ZookeeperClusterService service) {
        PinpointServerConfig config = mock(PinpointServerConfig.class);
        ChannelPropertiesFactory propertiesFactory = new ChannelPropertiesFactory();
        ServerStateChangeEventHandler eventHandler = new ClusterPointStateChangedEventHandler(propertiesFactory, service.getProfilerClusterManager());
        when(config.getStateChangeEventHandlers()).thenReturn(Arrays.asList(eventHandler));
        when(config.getServerStreamMessageHandler()).thenReturn(ServerStreamChannelMessageHandler.DISABLED_INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        TestServerMessageListenerFactory.TestServerMessageListener testServerMessageListener = TestServerMessageListenerFactory.create(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.ECHO);
        when(config.getMessageListener()).thenReturn(testServerMessageListener);
        when(config.getClusterOption()).thenReturn(ClusterOption.DISABLE_CLUSTER_OPTION);

        return config;
    }

}
