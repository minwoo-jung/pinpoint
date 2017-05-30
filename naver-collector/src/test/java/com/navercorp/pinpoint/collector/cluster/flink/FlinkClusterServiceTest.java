/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.cluster.zookeeper.DefaultZookeeperClient;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperTestUtils;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.service.SendAgentStatService;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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

    private static CollectorConfiguration config = null;

    @BeforeClass
    public static void setUp() {
        config = new CollectorConfiguration();
        config.setFlinkClusterEnable(true);
        config.setFlinkClusterZookeeperAddress("127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT);
        config.setFlinkClusterSessionTimeout(30000);
    }

    @Test
    public void addFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        PinpointServerAcceptor serverAcceptor = null;
        try {
            zookeeperServer = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            ZookeeperClient client = createZookeeperClient();

            int acceptorSocketPort = SocketUtils.findAvailableTcpPort(22214);
            serverAcceptor = createPinpointServerAcceptor(acceptorSocketPort);
            createZnode(client, acceptorSocketPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager);
            flinkClusterService.setUp();
            Thread.sleep(5000);

            List<PinpointSocket> writablePinpointServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList.size());
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());
        } finally {
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            closeServerAcceptor(serverAcceptor);
        }
    }

    @Test
    public void multiAddFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        List<PinpointServerAcceptor> serverAcceptorList = new ArrayList<>();

        try {
            zookeeperServer = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            ZookeeperClient client = createZookeeperClient();

            int acceptorSocketPort = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor = createPinpointServerAcceptor(acceptorSocketPort);
            serverAcceptorList.add(serverAcceptor);
            createZnode(client, acceptorSocketPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager);
            flinkClusterService.setUp();

            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList.size());
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            int acceptorSocketPort2 = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor2 = createPinpointServerAcceptor(acceptorSocketPort2);
            serverAcceptorList.add(serverAcceptor2);
            createZnode(client, acceptorSocketPort2);

            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList2 = serverAcceptor2.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList2.size());
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

        } finally {
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            closeServerAcceptorList(serverAcceptorList);
        }
    }

    @Test
    public void multiAddAndRemoveFlinkZnodeTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        TestingServer zookeeperServer = null;
        List<PinpointServerAcceptor> serverAcceptorList = new ArrayList<>();

        try {
            zookeeperServer = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            ZookeeperClient client = createZookeeperClient();

            int acceptorSocketPort = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor = createPinpointServerAcceptor(acceptorSocketPort);
            serverAcceptorList.add(serverAcceptor);
            createZnode(client, acceptorSocketPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager);
            flinkClusterService.setUp();

            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList.size());
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            int acceptorSocketPort2 = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor2 = createPinpointServerAcceptor(acceptorSocketPort2);
            serverAcceptorList.add(serverAcceptor2);
            createZnode(client, acceptorSocketPort2);
            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList2 = serverAcceptor2.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList2.size());
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            int acceptorSocketPort3 = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor3 = createPinpointServerAcceptor(acceptorSocketPort3);
            serverAcceptorList.add(serverAcceptor3);
            createZnode(client, acceptorSocketPort3);
            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList3 = serverAcceptor2.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList3.size());
            Assert.assertEquals(3, tcpDataSenderRepository.getAddressList().size());

            //remove znode test
            removeZnode(client, acceptorSocketPort3);

            Thread.sleep(5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

        } finally {
            closeFlinkClusterService(flinkClusterService);
            closeZookeeperServer(zookeeperServer);
            closeServerAcceptorList(serverAcceptorList);
        }
    }

    @Test
    public void zookeeperShutdownTest() throws Exception {
        FlinkClusterService flinkClusterService = null;
        List<PinpointServerAcceptor> serverAcceptorList = new ArrayList<>();

        try {
            TestingServer zookeeperServer = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
            ZookeeperClient client = createZookeeperClient();

            int acceptorSocketPort = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor = createPinpointServerAcceptor(acceptorSocketPort);
            serverAcceptorList.add(serverAcceptor);
            createZnode(client, acceptorSocketPort);

            SendAgentStatService sendAgentStatService = new SendAgentStatService(config);
            TcpDataSenderRepository tcpDataSenderRepository = new TcpDataSenderRepository(sendAgentStatService);
            FlinkClusterConnectionManager flinkClusterConnectionManager = new FlinkClusterConnectionManager(tcpDataSenderRepository);
            flinkClusterService = new FlinkClusterService(config, flinkClusterConnectionManager);
            flinkClusterService.setUp();

            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList.size());
            Assert.assertEquals(1, tcpDataSenderRepository.getAddressList().size());

            //add znode test
            int acceptorSocketPort2 = SocketUtils.findAvailableTcpPort(22214);
            PinpointServerAcceptor serverAcceptor2 = createPinpointServerAcceptor(acceptorSocketPort2);
            serverAcceptorList.add(serverAcceptor2);
            createZnode(client, acceptorSocketPort2);
            Thread.sleep(5000);
            List<PinpointSocket> writablePinpointServerList2 = serverAcceptor2.getWritableSocketList();
            Assert.assertEquals(1, writablePinpointServerList2.size());
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());

            //zookeeper shutdown test

            closeZookeeperServer(zookeeperServer);
            Thread.sleep(5000);
            Assert.assertEquals(2, tcpDataSenderRepository.getAddressList().size());
        } finally {
            closeFlinkClusterService(flinkClusterService);
            closeServerAcceptorList(serverAcceptorList);
        }
    }

    private void removeZnode(ZookeeperClient client, int acceptorSocketPort) throws PinpointZookeeperException, InterruptedException {
        client.delete(PINPOINT_FLINK_CLUSTER_PATH + "/" + "127.0.0.1:" + acceptorSocketPort);
    }

    private void closeFlinkClusterService(FlinkClusterService flinkClusterService) {
        try {
            if (flinkClusterService != null) {
                flinkClusterService.tearDown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createZnode(ZookeeperClient client, int acceptorSocketPort) throws Exception {
        client.createPath(PINPOINT_FLINK_CLUSTER_PATH, true);
        client.createNode(PINPOINT_FLINK_CLUSTER_PATH + "/" + "127.0.0.1:" + acceptorSocketPort, "127.0.0.1".getBytes());
    }

    private PinpointServerAcceptor createPinpointServerAcceptor(int acceptorSocketPort) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
        serverAcceptor.bind("127.0.0.1", acceptorSocketPort);
        return serverAcceptor;
    }

    private void closeServerAcceptor(PinpointServerAcceptor serverAcceptor) {
        try {
            if (serverAcceptor != null) {
                serverAcceptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeZookeeperServer(TestingServer zookeeperServer) {
        try {
            if (zookeeperServer != null) {
                zookeeperServer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeServerAcceptorList(List<PinpointServerAcceptor> serverAcceptorList) {
        for (PinpointServerAcceptor acceptor: serverAcceptorList) {
            closeServerAcceptor(acceptor);
        }
    }

    private ZookeeperClient createZookeeperClient() throws IOException {
        ZookeeperClient client = new DefaultZookeeperClient("127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT, 3000, new ZookeeperEventWatcher() {

            @Override
            public void process(WatchedEvent event) {

            }

            @Override
            public boolean isConnected() {
                return true;
            }

        });
        client.connect();
        return client;
    }
}