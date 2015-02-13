package com.navercorp.pinpoint.collector.cluster.zookeeper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.curator.test.TestingServer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.WritablePinpointServer;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperProfilerClusterServiceTest {

    private static final int DEFAULT_ACCEPTOR_PORT = 22213;

    private static CollectorConfiguration collectorConfig = null;

    private static Timer testTimer;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @BeforeClass
    public static void setUp() {
        collectorConfig = new CollectorConfiguration();

        collectorConfig.setClusterEnable(true);
        collectorConfig.setClusterAddress("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT);
        collectorConfig.setClusterSessionTimeout(3000);

        testTimer = TimerFactory.createHashedWheelTimer(ZookeeperProfilerClusterServiceTest.class.getSimpleName(), 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        testTimer.stop();
    }

    @Test
    public void simpleTest1() throws Exception {
        TestingServer ts = null;
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServer pinpointServer = createPinpointServer(service);
            
            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);

            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());

            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
            pinpointServer.messageReceived(handshakePacket);

            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());
            service.tearDown();
        } finally {
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
    @Test
    public void simpleTest2() throws Exception {
        TestingServer ts = null;
        try {
            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServer pinpointServer = createPinpointServer(service);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);

            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
            Thread.sleep(10000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
    // 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
    @Test
    public void simpleTest3() throws Exception {
        TestingServer ts = null;
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServer pinpointServer = createPinpointServer(service);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);
            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);
           
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            ts.stop();
            Thread.sleep(1000);
            ts.restart();
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
            closeZookeeperServer(ts);
        }
    }

    // 심플 쥬키퍼 테스트
    // 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
    @Test
    public void simpleTest4() throws Exception {
        TestingServer ts = null;
        try {
            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServer pinpointServer = createPinpointServer(service);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);
            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
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
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServer pinpointServer = createPinpointServer(service);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);
            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            ts.stop();
            Thread.sleep(1000);
            ts.restart();
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
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

    private Map<String, Object> getParams() {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");

        return properties;
    }
    
    private PinpointServer createPinpointServer(ZookeeperClusterService service) {
        Channel channel = mock(Channel.class);
        PinpointServerConfig config = createPinpointServerConfig(service);
        
        return new PinpointServer(channel, config);
    }

    private PinpointServerConfig createPinpointServerConfig(ZookeeperClusterService service) {
        PinpointServerConfig config = mock(PinpointServerConfig.class);
        when(config.getStateChangeEventHandler()).thenReturn(service.getChannelStateChangeEventHandler());
        when(config.getStreamMessageListener()).thenReturn(DisabledServerStreamChannelMessageListener.INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        when(config.getMessageListener()).thenReturn(new EchoServerListener());

        return config;
    }

    public static class EchoServerListener implements ServerMessageListener {
        private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
        private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();

        @Override
        public void handleSend(SendPacket sendPacket, WritablePinpointServer pinpointServer) {
            sendPacketRepository.add(sendPacket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, WritablePinpointServer pinpointServer) {
            requestPacketRepository.add(requestPacket);

            pinpointServer.response(requestPacket, requestPacket.getPayload());
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }
    }

}
