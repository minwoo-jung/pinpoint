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
import org.jboss.netty.util.Timeout;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Taejin Koo
 */
public class TokenLifeCycleManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenLifeCycleManagerTest.class);

    private final String tokenKey = "tokenKey";

    private final long intervalMillis = 1000;
    private final long additionalWaitTimeMillis = intervalMillis / 2;

    @Test
    public void deleteTest() {
        ZookeeperTokenDao zookeeperTokenDao = new MockZookeeperTokenDao();

        TokenLifeCycleManager tokenLifeCycleManager = new TokenLifeCycleManager(zookeeperTokenDao, intervalMillis);

        try {
            asserts(tokenLifeCycleManager, 1);
        } finally {
            tokenLifeCycleManager.stop();
        }
    }

    @Test
    public void deleteRetryTest() {
        int firstSuccessCount = 2;
        ZookeeperTokenDao zookeeperTokenDao = new MockZookeeperTokenDao(firstSuccessCount);

        TokenLifeCycleManager tokenLifeCycleManager = new TokenLifeCycleManager(zookeeperTokenDao, intervalMillis);

        try {
            asserts(tokenLifeCycleManager, firstSuccessCount);
        } finally {
            tokenLifeCycleManager.stop();
        }
    }

    @Test
    public void deleteCancelTest() {
        int firstSuccessCount = 5;
        ZookeeperTokenDao zookeeperTokenDao = new MockZookeeperTokenDao(firstSuccessCount);

        TokenLifeCycleManager tokenLifeCycleManager = new TokenLifeCycleManager(zookeeperTokenDao, intervalMillis);

        try {
            tokenLifeCycleManager.reserveDeleteTask(tokenKey, intervalMillis);
            tokenLifeCycleManager.cancelReserveDeleteTask(tokenKey);
            assertRemainTokenSize(tokenLifeCycleManager, 0);
        } finally {
            tokenLifeCycleManager.stop();
        }
    }

    private void asserts(TokenLifeCycleManager tokenLifeCycleManager, int firstSuccessCount) {
        tokenLifeCycleManager.reserveDeleteTask(tokenKey, intervalMillis);
        assertRemainTokenSize(tokenLifeCycleManager, 1);
        doSleep(additionalWaitTimeMillis);

        for (int i = 0; i < firstSuccessCount - 1; i++) {
            doSleep(intervalMillis);
            assertRemainTokenSize(tokenLifeCycleManager, 1);
        }

        doSleep(intervalMillis);
        assertRemainTokenSize(tokenLifeCycleManager, 0);
    }

    private void doSleep(long sleepTimes) {
        try {
            Thread.sleep(sleepTimes);
        } catch (InterruptedException e) {
        }
    }

    private void assertRemainTokenSize(TokenLifeCycleManager tokenLifeCycleManager, int expectedSize) {
        Map<String, Timeout> willBeDeleteTokenMap;
        willBeDeleteTokenMap = tokenLifeCycleManager.getWillBeDeleteTokenMap();
        Assert.assertEquals(expectedSize, willBeDeleteTokenMap.size());
    }

    private static class MockZookeeperTokenDao extends ZookeeperTokenDao {

        private final CountDownLatch firstSuccessCountLatch;

        MockZookeeperTokenDao() {
            this(1);
        }

        MockZookeeperTokenDao(int firstSuccessCount) {
            Assert.assertTrue(firstSuccessCount > 0);
            this.firstSuccessCountLatch = new CountDownLatch(firstSuccessCount);
        }

        @Override
        boolean deleteNode(String tokenKey) {
            firstSuccessCountLatch.countDown();

            if (firstSuccessCountLatch.getCount() == 0) {
                LOGGER.info("deleteNode success");
                return true;
            } else {
                LOGGER.info("deleteNode fail");
                return false;
            }
        }
    }

}
