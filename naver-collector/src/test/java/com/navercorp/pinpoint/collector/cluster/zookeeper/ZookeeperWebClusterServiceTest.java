package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperWebClusterServiceTest {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_WEB_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/web";

    private static final int DEFAULT_ZOOKEEPER_PORT = SocketUtils.findAvailableTcpPort(22213);

    private final TestServerMessageListenerFactory testServerMessageListenerFactory =
            new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.NO_RESPONSE);

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @Test
    public void simpleTest() throws Exception {
        TestingServer ts = null;
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            int bindPort = testPinpointServerAcceptor.bind();

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
            client.createPath(PINPOINT_WEB_CLUSTER_PATH + ZookeeperConstants.PATH_SEPARATOR);
            client.createNode(PINPOINT_WEB_CLUSTER_PATH + ZookeeperConstants.PATH_SEPARATOR + "127.0.0.1:" + bindPort, "127.0.0.1".getBytes());
            testPinpointServerAcceptor.assertAwaitClientConnected(1, 5000);

            client.close();
            testPinpointServerAcceptor.assertAwaitClientConnected(0, 5000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            testPinpointServerAcceptor.close();
            closeZookeeperServer(ts);
        }
    }

    private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
        try {
            mockZookeeperServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
