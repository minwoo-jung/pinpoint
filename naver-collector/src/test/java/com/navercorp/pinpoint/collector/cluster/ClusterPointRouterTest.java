package com.navercorp.pinpoint.collector.cluster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterServiceTest.EchoServerListener;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.DoNothingChannelStateEventHandler;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.TimerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest {

    private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = 22215;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long currentTime = System.currentTimeMillis();

    @Autowired
    ClusterPointRouter clusterPointRouter;

    private static Timer testTimer;

    @BeforeClass
    public static void setUp() {
        testTimer = TimerFactory.createHashedWheelTimer(ClusterPointRouterTest.class.getSimpleName(), 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        testTimer.stop();
    }

    @Test
    public void webClusterPointtest() {
        WebCluster webClusterPoint = new WebCluster(CollectorUtils.getServerIdentifier(), clusterPointRouter);

        try {
            PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.setMessageListener(new PinpointSocketManagerHandler());
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
            webClusterPoint.connectPointIfAbsent(address);
            Assert.assertEquals(1, webClusterPoint.getWebClusterList().size());
            webClusterPoint.connectPointIfAbsent(address);
            Assert.assertEquals(1, webClusterPoint.getWebClusterList().size());

            webClusterPoint.disconnectPoint(address);
            Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
            webClusterPoint.disconnectPoint(address);
            Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
            
            serverAcceptor.close();
        } finally {
            webClusterPoint.close();
        }
    }

    @Test
    public void profilerClusterPointtest() {
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

    private class PinpointSocketManagerHandler implements ServerMessageListener {
        @Override
        public void handleSend(SendPacket sendPacket, PinpointServer writablePinpointServer) {
            logger.warn("Unsupport send received {} {}", sendPacket, writablePinpointServer);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointServer writablePinpointServer) {
            logger.warn("Unsupport request received {} {}", requestPacket, writablePinpointServer);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.warn("do Handshake {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }
    }

    private Map<Object, Object> getParams() {
        Map<Object, Object> properties = new HashMap<Object, Object>();

        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), currentTime);
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0.3-SNAPSHOT");

        return properties;
    }

    private TargetClusterPoint findClusterPoint(String applicationName, String agentId, long startTimeStamp, List<TargetClusterPoint> targetClusterPointList) {

        List<TargetClusterPoint> result = new ArrayList<TargetClusterPoint>();

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
        when(config.getStateChangeEventHandler()).thenReturn(DoNothingChannelStateEventHandler.INSTANCE);
        when(config.getStreamMessageListener()).thenReturn(DisabledServerStreamChannelMessageListener.INSTANCE);
        when(config.getRequestManagerTimer()).thenReturn(testTimer);
        when(config.getDefaultRequestTimeout()).thenReturn((long) 1000);
        when(config.getMessageListener()).thenReturn(new EchoServerListener());

        return config;
    }

    
}
