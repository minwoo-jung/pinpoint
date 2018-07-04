package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionFactory;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionRepository;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnector;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterServiceTest.EchoServerListener;
import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.collector.util.DefaultAddress;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.server.handler.DoNothingChannelStateEventHandler;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest {

    private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = SocketUtils.findAvailableTcpPort(22215);

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

        try {
            clusterManager.start();

            PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.setMessageListenerFactory(new UnsupportedServerMessageListenerFactory());
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            Address address = new DefaultAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
            clusterManager.connectPointIfAbsent(address);
            Assert.assertEquals(1, clusterManager.getConnectedAddressList().size());
            clusterManager.connectPointIfAbsent(address);
            Assert.assertEquals(1, clusterManager.getConnectedAddressList().size());

            clusterManager.disconnectPoint(address);
            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
            clusterManager.disconnectPoint(address);
            Assert.assertEquals(0, clusterManager.getConnectedAddressList().size());
            
            serverAcceptor.close();
        } finally {
            clusterManager.stop();
        }
    }

    @Test
    public void profilerClusterPointTest() {
        ClusterPointRepository clusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();

        DefaultPinpointServer pinpointServer = createPinpointServer();
        pinpointServer.setChannelProperties(getParams());

        ClusterPoint clusterPoint = new PinpointServerClusterPoint(pinpointServer);

        clusterPointRepository.addClusterPoint(clusterPoint);
        List<TargetClusterPoint> clusterPointList = clusterPointRepository.getClusterPointList();

        Assert.assertEquals(1, clusterPointList.size());
        Assert.assertNull(findClusterPoint("a", "a", -1L, clusterPointList));
        Assert.assertNull(findClusterPoint("application", "a", -1L, clusterPointList));
        Assert.assertEquals(clusterPoint, findClusterPoint("application", "agent", currentTime, clusterPointList));

        boolean isAdd = clusterPointRepository.addClusterPoint(new PinpointServerClusterPoint(pinpointServer));
        Assert.assertFalse(isAdd);

        clusterPointRepository.removeClusterPoint(new PinpointServerClusterPoint(pinpointServer));
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

    private TargetClusterPoint findClusterPoint(String applicationName, String agentId, long startTimeStamp, List<TargetClusterPoint> targetClusterPointList) {

        List<TargetClusterPoint> result = new ArrayList<>();

        for (TargetClusterPoint targetClusterPoint : targetClusterPointList) {
            if (!targetClusterPoint.getApplicationName().equals(applicationName)) {
                continue;
            }

            if (!targetClusterPoint.getAgentId().equals(agentId)) {
                continue;
            }

            if (!(targetClusterPoint.getStartTimeStamp() == startTimeStamp)) {
                continue;
            }

            result.add(targetClusterPoint);
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
        when(config.getStateChangeEventHandlers()).thenReturn(Arrays.asList(DoNothingChannelStateEventHandler.INSTANCE));
        when(config.getStreamMessageListener()).thenReturn(DisabledServerStreamChannelMessageListener.INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        when(config.getMessageListener()).thenReturn(new EchoServerListener());

        return config;
    }

    
}
