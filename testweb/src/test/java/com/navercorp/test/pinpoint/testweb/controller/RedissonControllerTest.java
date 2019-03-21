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

import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class RedissonControllerTest {
    private static final String HOST = "10.113.160.166";
    private static final int PORT = 6390;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void list() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RList<String> list = redisson.getList("myList");
        list.add("1");
        list.add("2");
        list.add("3");

        List<String> secondList = new ArrayList<>();
        secondList.add("4");
        secondList.add("5");
        list.addAll(secondList);

        for (String string : list) {
            System.out.println("List " + string);
        }

        // fetch all objects
        List<String> allValues = list.readAll();
        // clear
        list.removeAll(allValues);

        redisson.shutdown();
    }

    @Test
    public void map() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + HOST + ":" + PORT);

        RedissonClient redisson = Redisson.create(config);
        RMap<String, Integer> map = redisson.getMap("myMap");
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Integer value = map.get("c");
        System.out.println("value " + value);

//        Set<String> keys = new HashSet<>();
//        keys.add("a");
//        keys.add("b");
//        keys.add("c");
//        Map<String, Integer> mapSlice = map.getAll(keys);
//        System.out.println("Slice " + mapSlice);


//        Set<String> allKeys = map.readAllKeySet();
//        System.out.println("All keys " + allKeys);
//
//        Collection<Integer> allValues = map.readAllValues();
//        System.out.println("All values " + allValues);
//
//        map.remove("a");
//        map.remove("b");
//        map.remove("c");

        redisson.shutdown();
    }

}