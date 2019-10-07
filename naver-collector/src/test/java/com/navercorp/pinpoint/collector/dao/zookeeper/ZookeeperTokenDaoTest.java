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

package com.navercorp.pinpoint.collector.dao.zookeeper;

import com.navercorp.pinpoint.collector.service.TokenConfig;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.grpc.security.TokenType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */

@RunWith(MockitoJUnitRunner.Silent.class)
@ActiveProfiles("tokenAuthentication")
public class ZookeeperTokenDaoTest {

    private MockZookeeper mockZookeeper;

    @Mock
    private TokenConfig tokenConfig;

    @Autowired
    @InjectMocks
    ZookeeperTokenDao tokenDao1;

    @Autowired
    @InjectMocks
    ZookeeperTokenDao tokenDao2;

    @Before
    public void setUp() throws Exception {
        int availablePort = SocketUtils.findAvailableTcpPort(22213);

        mockZookeeper = new MockZookeeper(availablePort);
        mockZookeeper.setUp();

        when(tokenConfig.getAddress()).thenReturn("127.0.0.1:" + availablePort);
        when(tokenConfig.getPath()).thenReturn("/test-path");
        when(tokenConfig.getSessionTimeout()).thenReturn(3000);
        when(tokenConfig.getOperationRetryInterval()).thenReturn(5000L);
        when(tokenConfig.getTtl()).thenReturn(5000L);

        tokenDao1.start();
        tokenDao2.start();
    }

    @After
    public void tearDown() throws Exception {
        tokenDao1.stop();
        tokenDao2.stop();

        mockZookeeper.tearDown();
    }

    // 1 create -> 2 get -> 1 get fail
    @Test
    public void createAndRemoveTest1() throws IOException {
        Token newToken = createNewToken();
        tokenDao1.create(newToken);

        doSleep(100);

        Token token = tokenDao2.getAndRemove(newToken.getKey());
        Assert.assertNotNull(token);

        token = tokenDao1.getAndRemove(newToken.getKey());
        Assert.assertNull(token);
    }

    // 1 create -> 1 get -> 2 get fail
    @Test
    public void createAndRemoveTest2() throws IOException {
        Token newToken = createNewToken();
        tokenDao1.create(newToken);

        doSleep(100);

        Token token = tokenDao1.getAndRemove(newToken.getKey());
        Assert.assertNotNull(token);

        token = tokenDao2.getAndRemove(newToken.getKey());
        Assert.assertNull(token);
    }

    private void doSleep(long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (InterruptedException e) {
        }
    }

    private final AtomicInteger integer = new AtomicInteger();

    private Token createNewToken() {
        String key = UUID.randomUUID().toString();

        String namespace = "namespace" + integer.incrementAndGet();

        long startTime = System.currentTimeMillis();
        long expiryTime = startTime + 5 * 1000;

        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("org", "namespace", "hbaseNamespace");

        Token token = new Token("key", paaSOrganizationInfo, expiryTime, "127.0.0.1", TokenType.ALL);
        return token;
    }

}
