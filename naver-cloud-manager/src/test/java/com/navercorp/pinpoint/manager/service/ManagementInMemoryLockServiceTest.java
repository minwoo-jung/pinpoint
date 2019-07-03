/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.manager.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HyunGil Jeong
 */
public class ManagementInMemoryLockServiceTest {

    private final ManagementInMemoryLockService service = new ManagementInMemoryLockService();

    @Test
    public void acquireShouldSucceedOnSameMonitor() {
        final UUID monitor = UUID.randomUUID();
        Assert.assertTrue(service.acquire("organizationKey", monitor));
        Assert.assertTrue(service.acquire("organizationKey", monitor));
    }

    @Test
    public void acquireShouldFailOnDifferentMonitors() {
        final UUID monitor1 = UUID.randomUUID();
        final UUID monitor2 = UUID.randomUUID();
        Assert.assertTrue(service.acquire("organizationKey", monitor1));
        Assert.assertFalse(service.acquire("organizationKey", monitor2));
    }

    @Test
    public void acquireShouldSucceedOnDifferentOrganizationKeys() {
        final UUID monitor = UUID.randomUUID();
        Assert.assertTrue(service.acquire("organizationKey1", monitor));
        Assert.assertTrue(service.acquire("organizationKey2", monitor));
    }

    @Test
    public void finalReleaseShouldRemoveOrganizationKey() {
        final int acquireCount = 5;
        final int releaseCount = acquireCount;
        final UUID monitor = UUID.randomUUID();
        for (int i = 0; i < acquireCount; i++) {
            service.acquire("organizationKey", monitor);
        }
        for (int i = 0; i < releaseCount; i++) {
            service.release("organizationKey", monitor);
        }
        Assert.assertFalse(service.reset("organizationKey"));
    }

    @Test
    public void nonFinalReleaseShouldNotRemoveOrganizationKey() {
        final int acquireCount = 5;
        final int releaseCount = acquireCount - 1;
        final UUID monitor = UUID.randomUUID();
        for (int i = 0; i < acquireCount; i++) {
            service.acquire("organizationKey", monitor);
        }
        for (int i = 0; i < releaseCount; i++) {
            service.release("organizationKey", monitor);
        }
        Assert.assertTrue(service.reset("organizationKey"));
    }

    @Test
    public void concurrentCallsShouldSucceed() throws InterruptedException {
        final UUID monitor = UUID.randomUUID();
        final int concurrency = 100;
        final CountDownLatch readyLatch = new CountDownLatch(concurrency);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
        final CompletableFuture[] completableFutures = new CompletableFuture[concurrency];
        for (int i = 0; i < concurrency; i++) {
            completableFutures[i] = CompletableFuture.runAsync(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
                Assert.assertTrue(service.acquire("organizationKey", monitor));
                service.release("organizationKey", monitor);
            }, executorService);
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        CompletableFuture.allOf(completableFutures).join();
        executorService.shutdownNow();

        Assert.assertFalse(service.reset("organizationKey"));
    }

    @Test
    public void organizationKeyShouldOnlyAcquireTheLockOnce() throws InterruptedException {
        final int concurrency = 20;
        final AtomicInteger acquireSuccessCounter = new AtomicInteger();
        final AtomicInteger acquireFailCounter = new AtomicInteger();
        final CountDownLatch readyLatch = new CountDownLatch(concurrency);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(concurrency);

        final CompletableFuture[] completableFutures = new CompletableFuture[concurrency];
        for (int i = 0; i < concurrency; i++) {
            completableFutures[i] = CompletableFuture.runAsync(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
                if (service.acquire("organizationKey", UUID.randomUUID())) {
                    acquireSuccessCounter.incrementAndGet();
                } else {
                    acquireFailCounter.incrementAndGet();
                }
            }, executorService);
        }
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        CompletableFuture.allOf(completableFutures).join();
        executorService.shutdownNow();

        Assert.assertEquals(1, acquireSuccessCounter.get());
        Assert.assertEquals(concurrency - 1, acquireFailCounter.get());
        Assert.assertTrue(service.reset("organizationKey"));
    }
}
