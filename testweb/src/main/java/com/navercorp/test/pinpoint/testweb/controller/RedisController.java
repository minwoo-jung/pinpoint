/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.nhncorp.redis.cluster.gateway.GatewayClient;
import com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline;
//import com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline;

@Controller
public class RedisController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String HOST = "10.113.160.166";
    private static final int PORT = 6390;

    @Autowired
    private GatewayClient client;

    // @Autowired
    // private StringRedisClusterTemplate redisTemplate;

    @RequestMapping(value = "/redis/jedis")
    @ResponseBody
    public String jedis(Model model) {
        logger.info("/redis/jedis");

        final Jedis jedis = new Jedis(HOST, PORT);

        jedis.get("foo");
        // jedis.close();

        return "OK";
    }

    @RequestMapping(value = "/redis/jedis/pipeline")
    @ResponseBody
    public String jedisPipeline(Model model) {
        logger.info("/redis/jedis/pipeline - add, update, get, delete");

        final Jedis jedis = new Jedis(HOST, PORT);
        Pipeline pipeline = jedis.pipelined();

        pipeline.set("foo", "bar");
        pipeline.get("foo");
        pipeline.expire("foo", 1);
        pipeline.syncAndReturnAll();

        // jedis.close();

        return "OK";
    }

    @RequestMapping(value = "/redis/nBaseArc")
    @ResponseBody
    public String nBaseArc(Model model) {
        logger.info("/redis/nBaseArc");

        client.get("foo");

        return "OK";
    }

    @RequestMapping(value = "/redis/nBaseArc/pipeline")
    @ResponseBody
    public String nBaseArcPipeline(Model model) {
        logger.info("/redis/nBaseArc/pipeline");

        RedisClusterPipeline pipeline = null;
        try {
            pipeline = client.pipeline();
            pipeline.set("foo", "bar");
            pipeline.get("foo");
            pipeline.expire("foo", 1);
            pipeline.syncAndReturnAll();
        } finally {
            if (pipeline != null) {
                pipeline.close();
            }
        }

        return "OK";
    }

    @RequestMapping(value = "/redis/nBaseArc/timeoutWaitingForIdle")
    @ResponseBody
    public String nBaseArcTimeoutWaitingForIdle(Model model) {
        logger.info("/redis/nBaseArc/timeoutWaitingForIdle");

        List<RedisClusterPipeline> pipelines = new ArrayList<RedisClusterPipeline>();
        try {
            for (int i = 0; i < 16; i++) {
                pipelines.add(client.pipeline());
            }
            client.get("foo");
        } finally {
            for (RedisClusterPipeline pipeline : pipelines) {
                pipeline.close();
            }
        }

        return "OK";
    }
}