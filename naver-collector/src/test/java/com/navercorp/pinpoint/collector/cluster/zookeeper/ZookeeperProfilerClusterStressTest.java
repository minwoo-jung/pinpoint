package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.apache.curator.test.TestingServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperProfilerClusterStressTest {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperProfilerClusterStressTest.class);

    private static final int DEFAULT_ACCEPTOR_PORT = SocketUtils.findAvailableTcpPort(22313);
    private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = SocketUtils.findAvailableTcpPort(22315);

    private static final MessageListener messageListener = ZookeeperTestUtils.getMessageListener();

    private static CollectorConfiguration collectorConfig = null;

    private final int doCount = 10;

    @Autowired
    ClusterPointRouter clusterPointRouter;

    @BeforeClass
    public static void setUp() {
        collectorConfig = new CollectorConfiguration();

        collectorConfig.setClusterEnable(true);
        collectorConfig.setClusterAddress("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT);
        collectorConfig.setClusterSessionTimeout(3000);
    }

    @Test
    public void simpleTest1() throws Exception {
        List<TestSocket> socketList = new ArrayList<>();

        PinpointServerAcceptor serverAcceptor = null;

        TestingServer ts = null;
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            ZookeeperProfilerClusterManager profiler = service.getProfilerClusterManager();

            serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.addStateChangeEventHandler(service.getChannelStateChangeEventHandler());
            serverAcceptor.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            socketList = connectPoint(socketList, address, doCount);
            Thread.sleep(1000);
            Assert.assertEquals(socketList.size(), profiler.getClusterData().size());

            for (int i=0; i < doCount; i++) {
                socketList = randomJob(socketList, address);
                Thread.sleep(1000);
                Assert.assertEquals(socketList.size(), profiler.getClusterData().size());
            }

            disconnectPoint(socketList, socketList.size());
            Thread.sleep(1000);
            Assert.assertEquals(0, profiler.getClusterData().size());

            service.tearDown();
        } finally {
            closeZookeeperServer(ts);
            serverAcceptor.close();
        }
    }

    @Test
    public void simpleTest2() throws Exception {

        PinpointServerAcceptor serverAcceptor = null;

        TestingServer ts = null;
        try {
            ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

            ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
            service.setUp();

            ZookeeperProfilerClusterManager profiler = service.getProfilerClusterManager();

            serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.addStateChangeEventHandler(service.getChannelStateChangeEventHandler());
            serverAcceptor.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
            serverAcceptor.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

            CountDownLatch latch = new CountDownLatch(2);


            List<TestSocket> socketList1 = connectPoint(new ArrayList<TestSocket>(), address, doCount);
            List<TestSocket> socketList2 = connectPoint(new ArrayList<TestSocket>(), address, doCount);


            WorkerJob job1 = new WorkerJob(latch, socketList1, address, 5);
            WorkerJob job2 = new WorkerJob(latch, socketList2, address, 5);

            Thread worker1 = new Thread(job1);
            worker1.setDaemon(false);
            worker1.start();

            Thread worker2 = new Thread(job2);
            worker2.setDaemon(false);
            worker2.start();


            latch.await();

            List<TestSocket> socketList = new ArrayList<>();
            socketList.addAll(job1.getSocketList());
            socketList.addAll(job2.getSocketList());


            logger.info(profiler.getClusterData().toString());
            Assert.assertEquals(socketList.size(), profiler.getClusterData().size());


            disconnectPoint(socketList, socketList.size());
            Thread.sleep(1000);

            service.tearDown();
        } finally {
            closeZookeeperServer(ts);
            serverAcceptor.close();
        }
    }

    class WorkerJob implements Runnable {

        private final CountDownLatch latch;

        private final InetSocketAddress address;

        private List<TestSocket> socketList;
        private int workCount;

        public WorkerJob(CountDownLatch latch, List<TestSocket> socketList, InetSocketAddress address ,int workCount) {
            this.latch = latch;
            this.address = address;

            this.socketList = socketList;
            this.workCount = workCount;
        }

        @Override
        public void run() {
            try {
                for (int i=0; i < workCount; i++) {
                    socketList = randomJob(socketList, address);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            } finally {
                latch.countDown();
            }
        }

        public List<TestSocket> getSocketList() {
            return socketList;
        }

    }

    private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
        try {
            mockZookeeperServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TestSocket {

        private final PinpointClientFactory factory;
        private final Map<String, Object> properties;

        private PinpointClient socket;


        public TestSocket() {
            this.properties = ZookeeperTestUtils.getParams(Thread.currentThread().getName(), "agent", System.currentTimeMillis());

            this.factory = new DefaultPinpointClientFactory();
            this.factory.setProperties(properties);
            this.factory.setMessageListener(messageListener);
        }

        private void connect(InetSocketAddress address) {
            if (socket == null) {
                socket = createPinpointClient(factory, address);
            }
        }

        private void stop() {
            if (socket != null) {
                socket.close();
                socket = null;
            }

            if (factory != null) {
                factory.release();
            }
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "(" + properties + ")";
        }

    }

    private PinpointClient createPinpointClient(PinpointClientFactory clientFactory, InetSocketAddress address) {
        String host = address.getHostName();
        int port = address.getPort();

        PinpointClient socket = null;
        for (int i = 0; i < 3; i++) {
            try {
                socket = clientFactory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = clientFactory.scheduledConnect(host, port);

        return socket;
    }

    private List<TestSocket> randomJob(List<TestSocket> socketList, InetSocketAddress address) {
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = Math.abs(random.nextInt());

        if (randomNumber % 2 == 0) {
            return connectPoint(socketList, address, 1);
        } else {
            return disconnectPoint(socketList, 1);
        }
    }

    private List<TestSocket> connectPoint(List<TestSocket> socketList, InetSocketAddress address, int count) {
//        logger.info("connect list=({}), address={}, count={}.", socketList, address, count);

        for (int i = 0; i < count; i++) {

            // startTimeStamp 혹시나 안겹치게
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TestSocket socket = new TestSocket();
            logger.info("connect ({}), .", socket);
            socket.connect(address);

            socketList.add(socket);
        }

        return socketList;
    }

    private List<TestSocket> disconnectPoint(List<TestSocket> socketList, int count) {
//        logger.info("disconnect list=({}), count={}.", socketList, count);

        int index = 1;

        Iterator<TestSocket> iterator = socketList.iterator();
        while (iterator.hasNext()) {
            TestSocket socket = iterator.next();

            logger.info("disconnect ({}), .", socket);
            socket.stop();

            iterator.remove();

            if (index++ >= count) {
                break;
            }
        }

        return socketList;
    }

}
