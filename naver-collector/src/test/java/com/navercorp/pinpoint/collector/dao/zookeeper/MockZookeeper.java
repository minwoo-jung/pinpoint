package com.navercorp.pinpoint.collector.dao.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterTestUtils;
import org.apache.curator.test.TestingServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Taejin Koo
 */
public class MockZookeeper {

    private final int bindPort;
    private TestingServer mockZookeeperServer = null;

    public MockZookeeper(int bindPort) {
        this.bindPort = bindPort;
    }

    @PostConstruct
    public void setUp() throws Exception {
        mockZookeeperServer = ClusterTestUtils.createZookeeperServer(bindPort);
    }

    @PreDestroy
    public void tearDown() throws Exception {
        if (mockZookeeperServer != null) {
            mockZookeeperServer.stop();
        }
    }

}
