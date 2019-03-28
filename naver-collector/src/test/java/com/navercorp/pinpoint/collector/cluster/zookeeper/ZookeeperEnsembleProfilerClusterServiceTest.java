package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingZooKeeperServer;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperEnsembleProfilerClusterServiceTest {

    private static Timer testTimer;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @BeforeClass
    public static void setUp() {
        testTimer = TimerFactory.createHashedWheelTimer(ZookeeperEnsembleProfilerClusterServiceTest.class.getSimpleName(), 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        testTimer.stop();
    }

    @Test
    public void simpleTest1() throws Exception {
        TestingCluster tcluster = null;
        ZookeeperClusterService service = null;
        try {
            tcluster = createZookeeperCluster(3);
            service = ClusterTestUtils.createZookeeperClusterService(tcluster.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperCluster(tcluster);
        }
    }

    // 연결되어 있는 쥬키퍼 클러스터가 끊어졌을때 해당 이벤트가 유지되는지
    // 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
    @Test
    public void simpleTest2() throws Exception {
        TestingCluster tcluster = null;
        ZookeeperClusterService service = null;
        try {
            tcluster = createZookeeperCluster(3);
            service = ClusterTestUtils.createZookeeperClusterService(tcluster.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            restart(tcluster);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperCluster(tcluster);
        }
    }


    // 연결되어 있는 쥬키퍼 클러스터가 모두 죽었을 경우
    // 그 이후 해당 이벤트가 유지되는지
    // 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
    @Test
    public void simpleTest3() throws Exception {
        TestingCluster tcluster = null;
        ZookeeperClusterService service = null;
        try {
            tcluster = createZookeeperCluster(3);
            service = ClusterTestUtils.createZookeeperClusterService(tcluster.getConnectString(), clusterPointRouter);

            DefaultPinpointServer pinpointServer = ClusterTestUtils.createPinpointServer(createPinpointServerConfig(service));
            Thread.sleep(1000);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0);

            stateToDuplex(pinpointServer);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1, 1000);

            tcluster.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);

            restart(tcluster);
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 1);

            pinpointServer.stop();
            ClusterTestUtils.assertConnectedClusterSize(profilerClusterManager, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperCluster(tcluster);
        }
    }

    private TestingCluster createZookeeperCluster(int size) throws Exception {
        return createZookeeperCluster(size, true);
    }

    private TestingCluster createZookeeperCluster(int size, boolean start) throws Exception {
        TestingCluster zookeeperCluster = new TestingCluster(size);

        // 주의 cluster 초기화에 시간이 좀 걸림 그래서 테스트에 sleep을 좀 길게 둠
        // 다 된걸 받는 이벤트도 없음
        if (start) {
            zookeeperCluster.start();
            Thread.sleep(5000);
        }

        return zookeeperCluster;
    }

    private void restart(TestingCluster zookeeperCluster) throws Exception {
        for (TestingZooKeeperServer zookeeperServer : zookeeperCluster.getServers()) {
            zookeeperServer.restart();
        }
        Thread.sleep(5000);
    }

    private void closeZookeeperCluster(TestingCluster zookeeperCluster) throws Exception {
        try {
            if (zookeeperCluster != null) {
                zookeeperCluster.close();
            }
        } catch (Exception ignored) {
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
        when(config.getStateChangeEventHandlers()).thenReturn(Arrays.asList(service.getChannelStateChangeEventHandler()));
        when(config.getServerStreamMessageHandler()).thenReturn(ServerStreamChannelMessageHandler.DISABLED_INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        TestServerMessageListenerFactory.TestServerMessageListener testServerMessageListener = TestServerMessageListenerFactory.create(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.ECHO);
        when(config.getMessageListener()).thenReturn(testServerMessageListener);
        when(config.getClusterOption()).thenReturn(ClusterOption.DISABLE_CLUSTER_OPTION);

        return config;
    }

}
