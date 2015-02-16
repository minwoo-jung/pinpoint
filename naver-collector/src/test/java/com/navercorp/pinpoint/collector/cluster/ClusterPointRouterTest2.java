package com.navercorp.pinpoint.collector.cluster;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.jboss.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest2 {

    private static final int DEFAULT_COLLECTOR_ACCEPTOR_SOCKET_PORT = 22214;
    private static final int DEFAULT_WEB_ACCEPTOR_SOCKET_PORT = 22215;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long currentTime = System.currentTimeMillis();

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @Autowired
    private SerializerFactory commandSerializerFactory;

    @Autowired
    private DeserializerFactory commandDeserializerFactory;

    @Test
    public void profilerClusterPointtest() throws TException, InterruptedException {
        WebCluster webCluster = null;
        PinpointSocketFactory agentFactory = null;
        PinpointServerAcceptor collectorAcceptor = null;
        PinpointServerAcceptor webAcceptor = null;
        try {
            webCluster = new WebCluster(CollectorUtils.getServerIdentifier(), clusterPointRouter);
            
            agentFactory = createSocketFactory();
            collectorAcceptor = createServerAcceptor("127.0.0.1", DEFAULT_COLLECTOR_ACCEPTOR_SOCKET_PORT);
            agentFactory.connect("127.0.0.1", DEFAULT_COLLECTOR_ACCEPTOR_SOCKET_PORT);
            
            Thread.sleep(100);
            
            List<PinpointServer> writablePinpointServerList = collectorAcceptor.getWritableServerList();

            for (PinpointServer writablePinpointServer : writablePinpointServerList) {
                ClusterPoint clusterPoint = new PinpointServerClusterPoint((DefaultPinpointServer)writablePinpointServer);
                
                ClusterPointRepository clusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();
                clusterPointRepository.addClusterPoint(clusterPoint);
            }

            webAcceptor = createServerAcceptor("127.0.0.1", DEFAULT_WEB_ACCEPTOR_SOCKET_PORT);
            
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_WEB_ACCEPTOR_SOCKET_PORT);
            webCluster.connectPointIfAbsent(address);
            
  
            byte[] echoPayload = createEchoPayload("hello");
            byte[] commandDeliveryPayload = createDeliveryCommandPayload("application", "agent", currentTime, echoPayload);

            List<PinpointServer> contextList = webAcceptor.getWritableServerList();
            PinpointServer writablePinpointServer = contextList.get(0);
            Future<ResponseMessage> future = writablePinpointServer.request(commandDeliveryPayload);
            future.await();

            TCommandEcho base = (TCommandEcho) SerializationUtils.deserialize(future.getResult().getMessage(), commandDeserializerFactory);
            Assert.assertEquals(base.getMessage(), "hello");
        } finally {
            if (webCluster != null) {
                webCluster.close();
            }
            
            if (agentFactory  != null) {
                agentFactory.release();
            }
            
            if (collectorAcceptor != null) {
                collectorAcceptor.close();
            }
            
            if (webAcceptor != null) {
                webAcceptor.close();
            }
        }
    }

    private PinpointServerAcceptor createServerAcceptor(String host, int port) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListener(new PinpointSocketManagerHandler());
        serverAcceptor.bind(host, port);


        return serverAcceptor;
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
            logger.warn("do handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
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
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), currentTime);
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0.3-SNAPSHOT");

        return properties;
    }

    private PinpointSocketFactory createSocketFactory() {
        PinpointSocketFactory factory = new PinpointSocketFactory();
        factory.setProperties(getParams());
        factory.setMessageListener(new EchoMessageListener());
        
        return factory;
    }
    
    class EchoMessageListener implements MessageListener {

        @Override
        public void handleSend(SendPacket sendPacket, Channel channel) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, Channel channel) {
            byte[] payload = requestPacket.getPayload();
            channel.write(new ResponsePacket(requestPacket.getRequestId(), requestPacket.getPayload()));
        }
        
    }
    
    
    
}
