package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class ZookeeperTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperTestUtils.class);

    private ZookeeperTestUtils() {
    }

    static MessageListener getMessageListener()  {
        return new SimpleMessageListener();
    }

    public static ServerMessageListener getServerMessageListener() {
        return new SimpleServerMessageListener();
    }

    static Map<String, Object> getParams() {
        return getParams("application", "agent", System.currentTimeMillis());
    }

    static Map<String, Object> getParams(String applicationName, String agentId, long startTimeMillis) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(HandshakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(HandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(HandshakePropertyType.IP.getName(), "ip");
        properties.put(HandshakePropertyType.PID.getName(), 1111);
        properties.put(HandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), startTimeMillis);
        properties.put(HandshakePropertyType.VERSION.getName(), "1.0");

        return properties;
    }

    public static TestingServer createZookeeperServer(int port) throws Exception {
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

    private static class SimpleServerMessageListener implements ServerMessageListener {
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
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            LOGGER.warn("Unsupported ping received packet:{}, remote:{}", pingPacket, pinpointServer);
        }

    }

}
