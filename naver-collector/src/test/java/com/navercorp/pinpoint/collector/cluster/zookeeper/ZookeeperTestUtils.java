package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.*;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

final class ZookeeperTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperTestUtils.class);

    private ZookeeperTestUtils() {
    }

    static MessageListener getMessageListener()  {
        return new SimpleMessageListener();
    }

    static ServerMessageListener getServerMessageListener() {
        return new SimpleServerMessageListner();
    }

    static Map<String, Object> getParams() {
        return getParams("application", "agent", System.currentTimeMillis());
    }

    static Map<String, Object> getParams(String applicationName, String agentId, long startTimeMillis) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), startTimeMillis);
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");

        return properties;
    }

    static TestingServer createZookeeperServer(int port) throws Exception {
        TestingServer mockZookeeperServer = new TestingServer(port);
        mockZookeeperServer.start();

        return mockZookeeperServer;
    }

    private static class SimpleMessageListener implements MessageListener {

        public SimpleMessageListener() {
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            LOGGER.info("Received SendPacket{} {}", sendPacket, pinpointSocket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            LOGGER.info("Received RequestPacket{} {}", requestPacket, pinpointSocket);
        }
    }

    private static class SimpleServerMessageListner implements ServerMessageListener {
        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            LOGGER.warn("Unsupport send received {} {}", sendPacket, pinpointSocket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            LOGGER.warn("Unsupport request received {} {}", requestPacket, pinpointSocket);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            LOGGER.warn("do handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPacket pingPacket, PinpointServer writablePinpointServer) {
            LOGGER.warn("Unsupport ping received {} {}", pingPacket, writablePinpointServer);
        }
    }

}
