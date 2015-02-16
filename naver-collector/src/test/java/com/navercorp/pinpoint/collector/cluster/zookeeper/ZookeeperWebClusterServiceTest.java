package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.util.List;

import junit.framework.Assert;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperWebClusterServiceTest {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_WEB_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/web";
    private static final String PINPOINT_PROFILER_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/profiler";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int DEFAULT_ZOOKEEPER_PORT = 22213;
    private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = 22214;

    private static CollectorConfiguration collectorConfig = null;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @BeforeClass
    public static void setUp() {
        collectorConfig = new CollectorConfiguration();

        collectorConfig.setClusterEnable(true);
        collectorConfig.setClusterAddress("127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT);
        collectorConfig.setClusterSessionTimeout(3000);
    }

    @Test
    public void simpleTest1() throws Exception {
        TestingServer ts = null;
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            ZookeeperClient client = new ZookeeperClient("127.0.0.1:" + DEFAULT_ZOOKEEPER_PORT, 3000, new ZookeeperEventWatcher() {

                @Override
                public void process(WatchedEvent event) {

                }

                @Override
                public boolean isConnected() {
                    return true;
                }
            });
            client.createPath(PINPOINT_WEB_CLUSTER_PATH, true);
            client.createNode(PINPOINT_WEB_CLUSTER_PATH + "/" + "127.0.0.1:" + DEFAULT_ACCEPTOR_SOCKET_PORT, "127.0.0.1".getBytes());

            Thread.sleep(5000);

            List<PinpointServer> writablePinpointServerList = serverAcceptor.getWritableServerList();
            Assert.assertEquals(1, writablePinpointServerList.size());

            client.close();

            Thread.sleep(5000);
            writablePinpointServerList = serverAcceptor.getWritableServerList();
            Assert.assertEquals(0, writablePinpointServerList.size());

            service.tearDown();
        } finally {
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
