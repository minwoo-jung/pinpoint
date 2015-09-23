package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.*;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        PinpointClientFactory agentFactory = null;
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
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.warn("Unsupport send received {} {}", sendPacket, pinpointSocket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.warn("Unsupport request received {} {}", requestPacket, pinpointSocket);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.warn("do handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPacket pingPacket, PinpointServer writablePinpointServer) {
            logger.warn("Unsupport ping received {} {}", pingPacket, writablePinpointServer);
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

    private PinpointClientFactory createSocketFactory() {
        PinpointClientFactory factory = new PinpointClientFactory();
        factory.setProperties(getParams());
        factory.setMessageListener(new EchoMessageListener());
        
        return factory;
    }
    
    class EchoMessageListener implements MessageListener {

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {

        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            byte[] payload = requestPacket.getPayload();
            pinpointSocket.response(requestPacket, payload);
        }
    }
    
    
    
}
