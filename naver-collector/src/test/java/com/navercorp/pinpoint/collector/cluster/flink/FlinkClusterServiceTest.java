/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import com.navercorp.pinpoint.collector.config.FlinkConfiguration;
import com.navercorp.pinpoint.collector.sender.FlinkRequestFactory;
import com.navercorp.pinpoint.collector.service.SendAgentStatService;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.FlinkTBaseLocator;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class FlinkClusterServiceTest {

    private static final int DEFAULT_ZOOKEEPER_PORT = SocketUtils.findAvailableTcpPort(22213);
    private static final String PINPOINT_FLINK_CLUSTER_PATH =  "/pinpoint-cluster/flink";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private FlinkConfiguration config;

    private final TestServerMessageListenerFactory testServerMessageListenerFactory =
            new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.NO_RESPONSE);

    @Before
    public void setUp() {
        config = new FlinkConfiguration(true, "127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT, 30000);
    }

    @Test
    public void addFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        ZookeeperClient client = null;
        try {
            zookeeperServer = ClusterTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            client = createZookeeperClient();

            int bindPort = testPinpointServerAcceptor.bind();
            createZnode(client, bindPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkTBaseLocator flinkTBaseLocator = new FlinkTBaseLocator(HeaderV1.VERSION);
            FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory = new FlinkHeaderTBaseSerializerFactory(flinkTBaseLocator.getTypeLocator());
            FlinkRequestFactory flinkRequestFactory = new FlinkRequestFactory();
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository, flinkHeaderTBaseSerializerFactory, flinkRequestFactory);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager, PINPOINT_FLINK_CLUSTER_PATH);
            flinkClusterService.setUp();

            testPinpointServerAcceptor.assertAwaitClientConnected(1, 5000);
            Assert.assertTrue(TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return 1 == tcpDataSenderRepository.getAddressList().size();
                }
            }, 100, 5000));
        } finally {
            closeZookeeperClient(client);
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void multiAddFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        List<TestPinpointServerAcceptor> testPinpointServerAcceptorList = new ArrayList<>();
        ZookeeperClient client = null;
        try {
            zookeeperServer = ClusterTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            client = createZookeeperClient();

            TestPinpointServerAcceptor testPinpointServerAcceptor1 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort = testPinpointServerAcceptor1.bind();

            testPinpointServerAcceptorList.add(testPinpointServerAcceptor1);
            createZnode(client, bindPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkTBaseLocator flinkTBaseLocator = new FlinkTBaseLocator(HeaderV1.VERSION);
            FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory = new FlinkHeaderTBaseSerializerFactory(flinkTBaseLocator.getTypeLocator());
            FlinkRequestFactory flinkRequestFactory = new FlinkRequestFactory();
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository, flinkHeaderTBaseSerializerFactory, flinkRequestFactory);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager, PINPOINT_FLINK_CLUSTER_PATH);
            flinkClusterService.setUp();

            testPinpointServerAcceptor1.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            TestPinpointServerAcceptor testPinpointServerAcceptor2 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort2 = testPinpointServerAcceptor2.bind();

            testPinpointServerAcceptorList.add(testPinpointServerAcceptor2);
            createZnode(client, bindPort2);

            testPinpointServerAcceptor2.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

        } finally {
            closeZookeeperClient(client);
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            closeServerAcceptorList(testPinpointServerAcceptorList);
        }
    }

    @Test
    public void multiAddAndRemoveFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        List<TestPinpointServerAcceptor> testPinpointServerAcceptorList = new ArrayList<>();
        ZookeeperClient client = null;
        try {
            zookeeperServer = ClusterTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            client = createZookeeperClient();

            TestPinpointServerAcceptor testPinpointServerAcceptor1 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort1 = testPinpointServerAcceptor1.bind();
            testPinpointServerAcceptorList.add(testPinpointServerAcceptor1);
            createZnode(client, bindPort1);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkTBaseLocator flinkTBaseLocator = new FlinkTBaseLocator(HeaderV1.VERSION);
            FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory = new FlinkHeaderTBaseSerializerFactory(flinkTBaseLocator.getTypeLocator());
            FlinkRequestFactory flinkRequestFactory = new FlinkRequestFactory();
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository, flinkHeaderTBaseSerializerFactory, flinkRequestFactory);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager, PINPOINT_FLINK_CLUSTER_PATH);
            flinkClusterService.setUp();

            testPinpointServerAcceptor1.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            TestPinpointServerAcceptor testPinpointServerAcceptor2 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort2 = testPinpointServerAcceptor2.bind();
            testPinpointServerAcceptorList.add(testPinpointServerAcceptor2);
            createZnode(client, bindPort2);

            testPinpointServerAcceptor2.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            TestPinpointServerAcceptor testPinpointServerAcceptor3 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort3 = testPinpointServerAcceptor3.bind();
            testPinpointServerAcceptorList.add(testPinpointServerAcceptor3);
            createZnode(client, bindPort3);
            testPinpointServerAcceptor3.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(3, tcpDataSenderRepository.getAddressList().size());

            //remove znode test
            removeZnode(client, bindPort3);

            Thread.sleep(5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());
        } finally {
            closeZookeeperClient(client);
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            closeServerAcceptorList(testPinpointServerAcceptorList);
        }
    }

    @Test
    public void zookeeperShutdownTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        List<TestPinpointServerAcceptor> testPinpointServerAcceptorList = new ArrayList<>();
        ZookeeperClient client = null;
        try {
            TestingServer zookeeperServer = ClusterTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            client = createZookeeperClient();

            TestPinpointServerAcceptor testPinpointServerAcceptor1 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort1 = testPinpointServerAcceptor1.bind();
            testPinpointServerAcceptorList.add(testPinpointServerAcceptor1);
            createZnode(client, bindPort1);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkTBaseLocator flinkTBaseLocator = new FlinkTBaseLocator(HeaderV1.VERSION);
            FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory = new FlinkHeaderTBaseSerializerFactory(flinkTBaseLocator.getTypeLocator());
            FlinkRequestFactory flinkRequestFactory = new FlinkRequestFactory();
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository, flinkHeaderTBaseSerializerFactory, flinkRequestFactory);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager, PINPOINT_FLINK_CLUSTER_PATH);
            flinkClusterService.setUp();

            testPinpointServerAcceptor1.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            TestPinpointServerAcceptor testPinpointServerAcceptor2 = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            int bindPort2 = testPinpointServerAcceptor2.bind();
            testPinpointServerAcceptorList.add(testPinpointServerAcceptor2);
            createZnode(client, bindPort2);

            testPinpointServerAcceptor2.assertAwaitClientConnected(1, 5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

            //zookeeper shutdown test

            closeZookeeperServer(zookeeperServer);
            Thread.sleep(5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());
        } finally {
            closeZookeeperClient(client);
            closeFlinkClusterService(flinkClusterService);
            closeServerAcceptorList(testPinpointServerAcceptorList);
        }
    }

    private void removeZnode(ZookeeperClient client, int acceptorSocketPort) throws PinpointZookeeperException, InterruptedException {
        client.delete(PINPOINT_FLINK_CLUSTER_PATH + "/" + "127.0.0.1:" + acceptorSocketPort);
    }

    private void closeZookeeperClient(ZookeeperClient client) {
        if (client != null) {
            client.close();
        }
    }

    private void closeFlinkClusterService(FlinkClusterService flinkClusterService) {
        try {
            if (flinkClusterService != null) {
                flinkClusterService.tearDown();
            }
        } catch (Exception e) {
            logger.error("exception has occurred while closeFlinkClusterService ", e);
        }
    }

    private void createZnode(ZookeeperClient client, int acceptorSocketPort) throws Exception {
        CreateNodeMessage createNodeMessage
                = new CreateNodeMessage(PINPOINT_FLINK_CLUSTER_PATH + ZookeeperConstants.PATH_SEPARATOR + "127.0.0.1:" + acceptorSocketPort, "127.0.0.1".getBytes(), true);
        client.createNode(createNodeMessage);
    }

    private void closeZookeeperServer(TestingServer zookeeperServer) {
        try {
            if (zookeeperServer != null) {
                zookeeperServer.close();
            }
        } catch (Exception e) {
            logger.error("exception has occurred while closeZookeeperServer ", e);
        }
    }

    private void closeServerAcceptorList(List<TestPinpointServerAcceptor> testPinpointServerAcceptorList) {
        for (TestPinpointServerAcceptor testPinpointServerAcceptor: testPinpointServerAcceptorList) {
            testPinpointServerAcceptor.close();
        }
    }

    private ZookeeperClient createZookeeperClient() throws IOException {
        ZookeeperClient client = new CuratorZookeeperClient("127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT, 3000, new ZookeeperEventWatcher() {

            @Override
            public void process(WatchedEvent event) {

            }

            @Override
            public boolean handleConnected() {
                return true;
            }

            @Override
            public boolean handleDisconnected() {
                return true;
            }

        });
        client.connect();
        return client;
    }
}