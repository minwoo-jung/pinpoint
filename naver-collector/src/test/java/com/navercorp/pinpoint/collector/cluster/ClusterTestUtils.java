/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerConfig;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.jboss.netty.channel.Channel;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public final class ClusterTestUtils {

    private ClusterTestUtils() {
    }

    public static Map<String, Object> getParams() {
        return getParams("application", "agent", System.currentTimeMillis());
    }

    public static Map<String, Object> getParams(String applicationName, String agentId, long startTimeMillis) {
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

    public static DefaultPinpointServer createPinpointServer(PinpointServerConfig config) {
        Channel channel = mock(Channel.class);

        DefaultPinpointServer pinpointServer = new DefaultPinpointServer(channel, config);
        pinpointServer.start();
        return pinpointServer;
    }


    public static void assertConnectedClusterSize(ProfilerClusterManager profilerClusterManager, int expectedConnectedClusterSize) {
        assertConnectedClusterSize(profilerClusterManager, expectedConnectedClusterSize, 0, 0);
    }

    public static void assertConnectedClusterSize(ProfilerClusterManager profilerClusterManager, int expectedConnectedClusterSize, long maxWaitTime) {
        assertConnectedClusterSize(profilerClusterManager, expectedConnectedClusterSize, 200, maxWaitTime);
    }

    public static void assertConnectedClusterSize(ProfilerClusterManager profilerClusterManager, int expectedConnectedClusterSize, long waitUnitTime, long maxWaitTime) {
        if (maxWaitTime > 0) {
            boolean await = TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return profilerClusterManager.getClusterData().size() == expectedConnectedClusterSize;
                }
            }, waitUnitTime, maxWaitTime);
            Assert.assertTrue(await);
        } else {
            boolean await = profilerClusterManager.getClusterData().size() == expectedConnectedClusterSize;
            Assert.assertTrue(await);
        }
    }

    public static ZookeeperClusterService createZookeeperClusterService(String connectString, ClusterPointRouter clusterPointRouter) throws InterruptedException, IOException, KeeperException {
        CollectorConfiguration collectorConfig = new CollectorConfiguration();
        collectorConfig.setClusterEnable(true);
        collectorConfig.setClusterAddress(connectString);
        collectorConfig.setClusterSessionTimeout(3000);

        ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
        service.setUp();

        return service;
    }

}
