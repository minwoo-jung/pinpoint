package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterPointStateChangedEventHandler;
import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import org.apache.curator.test.TestingServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperProfilerClusterStressTest {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperProfilerClusterStressTest.class);

    private static final int DEFAULT_ACCEPTOR_PORT = SocketUtils.findAvailableTcpPort(22313);
    private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = SocketUtils.findAvailableTcpPort(22315);

    private final TestServerMessageListenerFactory testServerMessageListenerFactory =
            new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, TestServerMessageListenerFactory.ResponseType.NO_RESPONSE);

    private final MessageListener messageListener = testServerMessageListenerFactory.create();

    private final int doCount = 10;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @Test
    public void simpleTest1() throws Exception {
        List<TestPinpointClient> testPinpointClientList = new ArrayList<>();

        PinpointServerAcceptor serverAcceptor = null;

        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            ProfilerClusterManager profiler = service.getProfilerClusterManager();

            serverAcceptor = new PinpointServerAcceptor();
            ChannelPropertiesFactory propertiesFactory = new ChannelPropertiesFactory();
            ServerStateChangeEventHandler eventHandler = new ClusterPointStateChangedEventHandler(propertiesFactory, profiler);
	        serverAcceptor.addStateChangeEventHandler(eventHandler);
            serverAcceptor.setMessageListenerFactory(testServerMessageListenerFactory);
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            testPinpointClientList = connectPoint(testPinpointClientList, address, doCount);
            ClusterTestUtils.assertConnectedClusterSize(profiler, testPinpointClientList.size(), 1000);

            for (int i = 0; i < doCount; i++) {
                testPinpointClientList = randomJob(testPinpointClientList, address);
                ClusterTestUtils.assertConnectedClusterSize(profiler, testPinpointClientList.size(), 1000);
            }

            disconnectPoint(testPinpointClientList, testPinpointClientList.size());
            ClusterTestUtils.assertConnectedClusterSize(profiler, 0, 1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);

            if (serverAcceptor != null) {
                serverAcceptor.close();
            }
        }
    }

    @Test
    public void simpleTest2() throws Exception {

        PinpointServerAcceptor serverAcceptor = null;

        TestingServer ts = null;
        ZookeeperClusterService service = null;
        try {
            ts = ClusterTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            service = ClusterTestUtils.createZookeeperClusterService(ts.getConnectString(), clusterPointRouter);

            ProfilerClusterManager profiler = service.getProfilerClusterManager();

            serverAcceptor = new PinpointServerAcceptor();
            ChannelPropertiesFactory propertiesFactory = new ChannelPropertiesFactory();
            ServerStateChangeEventHandler eventHandler = new ClusterPointStateChangedEventHandler(propertiesFactory, profiler);
	        serverAcceptor.addStateChangeEventHandler(eventHandler);
            serverAcceptor.setMessageListenerFactory(testServerMessageListenerFactory);
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            CountDownLatch latch = new CountDownLatch(2);


            List<TestPinpointClient> testPinpointClientList1 = connectPoint(new ArrayList<TestPinpointClient>(), address, doCount);
            List<TestPinpointClient> testPinpointClientList2 = connectPoint(new ArrayList<TestPinpointClient>(), address, doCount);


            WorkerJob job1 = new WorkerJob(latch, testPinpointClientList1, address, 5);
            WorkerJob job2 = new WorkerJob(latch, testPinpointClientList2, address, 5);

            Thread worker1 = new Thread(job1);
            worker1.setDaemon(false);
            worker1.start();

            Thread worker2 = new Thread(job2);
            worker2.setDaemon(false);
            worker2.start();


            latch.await();

            List<TestPinpointClient> testPinpointClientList = new ArrayList<>();
            testPinpointClientList.addAll(job1.getTestPinpointClientList());
            testPinpointClientList.addAll(job2.getTestPinpointClientList());


            logger.info(profiler.getClusterData().toString());
            Assert.assertEquals(testPinpointClientList.size(), profiler.getClusterData().size());


            disconnectPoint(testPinpointClientList, testPinpointClientList.size());
            Thread.sleep(1000);
        } finally {
            if (service != null) {
                service.tearDown();
            }
            closeZookeeperServer(ts);
            serverAcceptor.close();
        }
    }

    class WorkerJob implements Runnable {

        private final CountDownLatch latch;

        private final InetSocketAddress address;

        private List<TestPinpointClient> testPinpointClientList;
        private int workCount;

        public WorkerJob(CountDownLatch latch, List<TestPinpointClient> testPinpointClientList, InetSocketAddress address, int workCount) {
            this.latch = latch;
            this.address = address;

            this.testPinpointClientList = testPinpointClientList;
            this.workCount = workCount;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < workCount; i++) {
                    testPinpointClientList = randomJob(testPinpointClientList, address);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            } finally {
                latch.countDown();
            }
        }

        public List<TestPinpointClient> getTestPinpointClientList() {
            return testPinpointClientList;
        }

    }

    private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
        try {
            mockZookeeperServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<TestPinpointClient> randomJob(List<TestPinpointClient> testPinpointClientList, InetSocketAddress address) {
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = Math.abs(random.nextInt());

        if (randomNumber % 2 == 0) {
            return connectPoint(testPinpointClientList, address, 1);
        } else {
            return disconnectPoint(testPinpointClientList, 1);
        }
    }

    private List<TestPinpointClient> connectPoint(List<TestPinpointClient> testPinpointClientList, InetSocketAddress address, int count) {
//        logger.info("connect list=({}), address={}, count={}.", socketList, address, count);

        for (int i = 0; i < count; i++) {

            // startTimeStamp 혹시나 안겹치게
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            TestPinpointClient testPinpointClient = new TestPinpointClient(messageListener, ClusterTestUtils.getParams(Thread.currentThread().getName(), "agent", System.currentTimeMillis()));
            testPinpointClient.connect(address.getHostName(), address.getPort());

            testPinpointClientList.add(testPinpointClient);
        }

        return testPinpointClientList;
    }

    private List<TestPinpointClient> disconnectPoint(List<TestPinpointClient> testPinpointClientList, int count) {
//        logger.info("disconnect list=({}), count={}.", socketList, count);

        int index = 1;

        Iterator<TestPinpointClient> iterator = testPinpointClientList.iterator();
        while (iterator.hasNext()) {
            TestPinpointClient testPinpointClient = iterator.next();

            logger.info("disconnect ({}), .", testPinpointClient);
            testPinpointClient.closeAll();

            iterator.remove();

            if (index++ >= count) {
                break;
            }
        }

        return testPinpointClientList;
    }

}
