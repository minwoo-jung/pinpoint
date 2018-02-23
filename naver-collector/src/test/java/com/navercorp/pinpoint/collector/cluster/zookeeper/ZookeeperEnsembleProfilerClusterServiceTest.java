package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterServiceTest.EchoServerListener;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingZooKeeperServer;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        try {
            tcluster = createZookeeperCluster(3);

            String connectString = getConnectString(tcluster);

            CollectorConfiguration collectorConfig = createConfig(connectString);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            DefaultPinpointServer pinpointServer = createPinpointServer(service);
            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);

            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(0, payload);
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
            closeZookeeperCluster(tcluster);
        }
    }

    // 연결되어 있는 쥬키퍼 클러스터가 끊어졌을때 해당 이벤트가 유지되는지
    // 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
    @Test
    public void simpleTest2() throws Exception {
        TestingCluster tcluster = null;
        try {
            tcluster = createZookeeperCluster(3);

            String connectString = getConnectString(tcluster);

            CollectorConfiguration collectorConfig = createConfig(connectString);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            DefaultPinpointServer pinpointServer = createPinpointServer(service);

            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);
            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(0, payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            restart(tcluster);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
            closeZookeeperCluster(tcluster);
        }
    }


    // 연결되어 있는 쥬키퍼 클러스터가 모두 죽었을 경우
    // 그 이후 해당 이벤트가 유지되는지
    // 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
    @Test
    public void simpleTest3() throws Exception {
        TestingCluster tcluster = null;
        try {
            tcluster = createZookeeperCluster(3);

            String connectString = getConnectString(tcluster);

            CollectorConfiguration collectorConfig = createConfig(connectString);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            DefaultPinpointServer pinpointServer = createPinpointServer(service);
            ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

            pinpointServer.start();
            Thread.sleep(1000);
            List<String> result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            byte[] payload = ControlMessageEncodingUtils.encode(getParams());
            ControlHandshakePacket handshakePacket = new ControlHandshakePacket(0, payload);
            pinpointServer.messageReceived(handshakePacket);
            Thread.sleep(1000);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            stop(tcluster);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            restart(tcluster);

            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(1, result.size());

            pinpointServer.stop();
            Thread.sleep(1000);
            result = profilerClusterManager.getClusterData();
            Assert.assertEquals(0, result.size());

            service.tearDown();
        } finally {
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

    private void startZookeeperCluster(TestingCluster zookeeperCluster) throws Exception {
        zookeeperCluster.start();
        Thread.sleep(5000);
    }

    private void restart(TestingCluster zookeeperCluster) throws Exception {
        for (TestingZooKeeperServer zookeeperServer : zookeeperCluster.getServers()) {
            zookeeperServer.restart();
        }
        Thread.sleep(5000);
    }

    private void stop(TestingCluster zookeeperCluster) throws Exception {
        zookeeperCluster.stop();
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

    private String getConnectString(TestingZooKeeperServer testingZooKeeperServer) {
        return testingZooKeeperServer.getInstanceSpec().getConnectString();
    }

    private String getConnectString(TestingCluster zookeeperCluster) {
        StringBuilder connectString = new StringBuilder();

        Iterator<InstanceSpec> instanceSpecIterator = zookeeperCluster.getInstances().iterator();
        while (instanceSpecIterator.hasNext()) {
            InstanceSpec instanceSpec = instanceSpecIterator.next();
            connectString.append(instanceSpec.getConnectString());

            if (instanceSpecIterator.hasNext()) {
                connectString.append(",");
            }
        }

        return connectString.toString();
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
    
    private DefaultPinpointServer createPinpointServer(ZookeeperClusterService service) {
        Channel channel = mock(Channel.class);
        PinpointServerConfig config = createPinpointServerConfig(service);
        
        return new DefaultPinpointServer(channel, config);
    }

    private PinpointServerConfig createPinpointServerConfig(ZookeeperClusterService service) {
        PinpointServerConfig config = mock(PinpointServerConfig.class);
        when(config.getStateChangeEventHandlers()).thenReturn(Arrays.asList(service.getChannelStateChangeEventHandler()));
        when(config.getStreamMessageListener()).thenReturn(DisabledServerStreamChannelMessageListener.INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        when(config.getMessageListener()).thenReturn(new EchoServerListener());
        when(config.getClusterOption()).thenReturn(ClusterOption.DISABLE_CLUSTER_OPTION);

        return config;
    }


    private CollectorConfiguration createConfig(String connectString) {
        CollectorConfiguration collectorConfig = new CollectorConfiguration();

        collectorConfig.setClusterEnable(true);
        collectorConfig.setClusterAddress(connectString);
        collectorConfig.setClusterSessionTimeout(3000);

        return collectorConfig;
    }

}
