package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionFactory;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionRepository;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnector;
import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.collector.util.DefaultAddress;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long currentTime = System.currentTimeMillis();

    @Autowired
    ClusterPointRouter clusterPointRouter;

    private static String serverIdentifier;
    private static Timer testTimer;

    @BeforeClass
    public static void setUp() {
        serverIdentifier = CollectorUtils.getServerIdentifier();
        testTimer = TimerFactory.createHashedWheelTimer(ClusterPointRouterTest.class.getSimpleName(), 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        testTimer.stop();
    }

    @Test
    public void webClusterPointTest() {
        CollectorClusterConnectionRepository clusterRepository = new CollectorClusterConnectionRepository();
        CollectorClusterConnectionFactory clusterConnectionFactory = new CollectorClusterConnectionFactory(serverIdentifier, clusterPointRouter, clusterPointRouter);
        CollectorClusterConnector clusterConnector = clusterConnectionFactory.createConnector();

        CollectorClusterConnectionManager clusterManager = new CollectorClusterConnectionManager(serverIdentifier, clusterRepository, clusterConnector);

        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.NO_RESPONSE);
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        try {
            clusterManager.start();

            int bindPort = testPinpointServerAcceptor.bind();

            Address address = new DefaultAddress("127.0.0.1", bindPort);

            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
            clusterManager.connectPointIfAbsent(address);
            Assert.assertEquals(1, clusterManager.getConnectedAddressList().size());
            clusterManager.connectPointIfAbsent(address);
            Assert.assertEquals(1, clusterManager.getConnectedAddressList().size());

            clusterManager.disconnectPoint(address);
            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
            clusterManager.disconnectPoint(address);
            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
        } finally {
            testPinpointServerAcceptor.close();
            clusterManager.stop();
        }
    }

    @Test
    public void profilerClusterPointTest() {
        ClusterPointRepository clusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();

        DefaultPinpointServer pinpointServer = createPinpointServer();
        pinpointServer.setChannelProperties(getParams());

        ChannelPropertiesFactory factory = new ChannelPropertiesFactory();
        ChannelProperties channelProperties = factory.newChannelProperties(getParams());

        ClusterPoint clusterPoint = ThriftAgentConnection.newClusterPoint(pinpointServer, channelProperties);

        clusterPointRepository.addAndIsKeyCreated(clusterPoint);
        List<ClusterPoint> clusterPointList = clusterPointRepository.getClusterPointList();

        Assert.assertEquals(1, clusterPointList.size());
        Assert.assertNull(findClusterPoint("a", "a", -1L, clusterPointList));
        Assert.assertNull(findClusterPoint("application", "a", -1L, clusterPointList));
        Assert.assertEquals(clusterPoint, findClusterPoint("application", "agent", currentTime, clusterPointList));

        boolean isAdd = clusterPointRepository.addAndIsKeyCreated(ThriftAgentConnection.newClusterPoint(pinpointServer, channelProperties));
        Assert.assertFalse(isAdd);

        clusterPointRepository.removeAndGetIsKeyRemoved(ThriftAgentConnection.newClusterPoint(pinpointServer, channelProperties));
        clusterPointList = clusterPointRepository.getClusterPointList();

        Assert.assertEquals(0, clusterPointList.size());
        Assert.assertNull(findClusterPoint("application", "agent", currentTime, clusterPointList));
    }

    private Map<Object, Object> getParams() {
        Map<Object, Object> properties = new HashMap<>();

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

    private ClusterPoint findClusterPoint(String applicationName, String agentId, long startTimeStamp, List<ClusterPoint> targetClusterPointList) {

        List<ClusterPoint> result = new ArrayList<>();

        for (ClusterPoint targetClusterPoint : targetClusterPointList) {
            if (targetClusterPoint.getDestAgentInfo().equals(applicationName, agentId, startTimeStamp)) {
                result.add(targetClusterPoint);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        }

        if (result.size() > 1) {
            logger.warn("Ambiguous ClusterPoint {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeStamp, result);
            return null;
        }

        return null;
    }

    private DefaultPinpointServer createPinpointServer() {
        Channel channel = mock(Channel.class);
        PinpointServerConfig config = createPinpointServerConfig();

        return new DefaultPinpointServer(channel, config);
    }

    private PinpointServerConfig createPinpointServerConfig() {
        PinpointServerConfig config = mock(PinpointServerConfig.class);
        when(config.getStateChangeEventHandlers()).thenReturn(Arrays.asList(ServerStateChangeEventHandler.DISABLED_INSTANCE));
        when(config.getServerStreamMessageHandler()).thenReturn(ServerStreamChannelMessageHandler.DISABLED_INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        TestServerMessageListenerFactory.TestServerMessageListener testServerMessageListener = TestServerMessageListenerFactory.create(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.ECHO);
        when(config.getMessageListener()).thenReturn(testServerMessageListener);
        return config;
    }

}
