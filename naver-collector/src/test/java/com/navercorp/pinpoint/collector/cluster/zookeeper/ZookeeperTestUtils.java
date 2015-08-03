package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.util.HashMap;
import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.PinpointServer;

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
        public void handleSend(SendPacket sendPacket, Channel channel) {
            LOGGER.info("Received SendPacket{} {}", sendPacket, channel);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, Channel channel) {
            LOGGER.info("Received RequestPacket{} {}", requestPacket, channel);
        }
    }

    private static class SimpleServerMessageListner implements ServerMessageListener {
        @Override
        public void handleSend(SendPacket sendPacket, PinpointServer writablePinpointServer) {
            LOGGER.warn("Unsupport send received {} {}", sendPacket, writablePinpointServer);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointServer writablePinpointServer) {
            LOGGER.warn("Unsupport request received {} {}", requestPacket, writablePinpointServer);
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
