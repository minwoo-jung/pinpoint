/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.test.pinpoint.testweb.controller;

import org.redisson.Redisson;
import org.redisson.api.RBitSet;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RListReactive;
import org.redisson.api.RListRx;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
public class RedissonController {
    private static final String HOST = "10.113.160.166";
    private static final int PORT = 6390;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/redisson/cluster")
    @ResponseBody
    public String cluster() throws Exception {
        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://10.105.178.98:17000", "redis://10.105.178.98:17001", "redis://10.105.178.98:17002");

        RedissonClient redisson = Redisson.create(config);
        RList<String> list = redisson.getList("myList");
        list.add("1");

        redisson.shutdown();

        return "OK";
    }


    @RequestMapping(value = "/redisson/list")
    @ResponseBody
    public String list() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RList<String> list = redisson.getList("myList");
        list.add("1");
        list.add("2");
        list.add("3");

        RFuture<Boolean> future = list.addAsync("4");
        future.get();

        list.remove("1");

        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/deque")
    @ResponseBody
    public String blockingDeque() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RBlockingDeque<String> deque = redisson.getBlockingDeque("myQueue");
        deque.add("1");
        deque.add("2");

        deque.pollFirst();
        deque.pollLast();

        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/queue")
    @ResponseBody
    public String queue() throws Exception {
        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://10.105.178.98:17000", "redis://10.105.178.98:17001", "redis://10.105.178.98:17002");

        RedissonClient redisson = Redisson.create(config);
        RBlockingQueue<String> queue = redisson.getBlockingQueue("myQueue");
        queue.add("1");
        queue.add("2");

        queue.take();

        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/bucket")
    @ResponseBody
    public String bucket() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RBucket<String> bucket = redisson.getBucket("myBucket");
        bucket.set("123");
        bucket.getAndSet("321");
        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/latch")
    @ResponseBody
    public String latch() throws Exception {
        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://10.105.178.98:17000", "redis://10.105.178.98:17001", "redis://10.105.178.98:17002");

        RedissonClient redisson = Redisson.create(config);
        final RCountDownLatch latch = redisson.getCountDownLatch("myLatch");
        latch.trySetCount(1);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                }
                latch.countDown();
            }
        });
        latch.await(1000, TimeUnit.MILLISECONDS);
        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/bitSet")
    @ResponseBody
    public String bitSet() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RBitSet bs = redisson.getBitSet("myBitSet");
        bs.set(0, 5);
        bs.clear(0, 1);

        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/reactive")
    @ResponseBody
    public String reactive() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonReactiveClient redisson = Redisson.createReactive(config);
        RListReactive<String> list = redisson.getList("myList");
        list.add("1");
        list.add("2");
        list.add("3");

        Mono<String> mono = (Mono<String>) list.get(0);
        mono.block();

        redisson.shutdown();

        return "OK";
    }

    @RequestMapping(value = "/redisson/rx")
    @ResponseBody
    public String rx() throws Exception {
        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://10.105.178.98:17000", "redis://10.105.178.98:17001", "redis://10.105.178.98:17002");

        RedissonRxClient redisson = Redisson.createRx(config);
        RListRx<String> list = redisson.getList("myList");
        list.add("1");
        list.add("2");
        list.add("3");

        redisson.shutdown();

        return "OK";
    }
}