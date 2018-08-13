package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class ZookeeperTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperTestUtils.class);

    private ZookeeperTestUtils() {
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

}
