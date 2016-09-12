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

package com.navercorp.test.pinpoint.testweb.service;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author netspider
 */
@Service
public class CacheServiceImpl implements CacheService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int RANDOM_RANGE = 1000;
    @Autowired
    @Qualifier("arcusClientFactory")
    private ArcusClient arcus;

    @Autowired
    @Qualifier("memcachedClientFactory")
    private MemcachedClient memcached;

    private final Random random = new Random();


    public CacheServiceImpl() {
    }

    public void multiGetTest() {
        logger.info("multiGetTest");

        List<String> multiSetKey = multiSet(10);

        // get
        Future<Map<String, Object>> getFuture = null;
        try {
            getFuture  = arcus.asyncGetBulk(multiSetKey);
            final Map<String, Object> resultMap = getFuture.get(1000L, TimeUnit.MILLISECONDS);
            final Collection<Object> values = resultMap.values();
            logger.info("multiGet:{}", values);
        } catch (Exception e) {
            handelException(e, getFuture, "multiGet");
        }
        multiDelete(multiSetKey);
    }


    private void multiDelete(List<String> multiSetKey) {
        for (String key : multiSetKey) {

            // del
            Future<Boolean> delFuture = null;
            try {
                delFuture = arcus.delete(key);
                delFuture.get(1000L, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                handelException(e, delFuture, "get");
            }
        }

    }

    private List<String> multiSet(int retryCount) {
        final List<String> keyList = new ArrayList<>();

        for (int i = 0; i < retryCount; i++) {

            int rand = random.nextInt(RANDOM_RANGE);
            String key = "pinpoint:testkey-" + rand;
            keyList.add(key);

            // set
            Future<Boolean> setFuture = null;
            try {
                setFuture = arcus.set(key, 10, "Hello, pinpoint." + rand);
            } catch (Exception e) {
                handelException(e, setFuture, "set");
            }

        }
        return keyList;
    }

    @Override
    public void arcus() {
        int rand = random.nextInt(RANDOM_RANGE);
        String key = "pinpoint:testkey-" + rand;

        // set
        Future<Boolean> setFuture = null;
        try {
            setFuture = arcus.set(key, 10, "Hello, pinpoint." + rand);
            setFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, setFuture, "get");
        }

        // get
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(key);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, getFuture, "asyncGet");
        }

        // del
        Future<Boolean> delFuture = null;
        try {
            delFuture = arcus.delete(key);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, delFuture, "delete");
        }
    }



    @Override
    public void memcached() {
        int rand = random.nextInt(RANDOM_RANGE);
        String key = "pinpoint:testkey-" + rand;

        // set
        Future<Boolean> setFuture = null;
        try {
            setFuture = memcached.set(key, 10, "Hello, pinpoint." + rand);
            setFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, setFuture, "set");
        }

        // get
        Future<Object> getFuture = null;
        try {
            getFuture = memcached.asyncGet(key);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, getFuture, "asyncGet");
        }

        // del
        Future<Boolean> delFuture = null;
        try {
            delFuture = memcached.delete(key);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, delFuture, "delete");
        }
    }


    private void handelException(Exception e, Future<?> future, String message) {
        logException(e, future, "delete");
        cancelFuture(future);
    }

    private void cancelFuture(Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private void logException(Exception ex, Future future, String message) {
        if (ex != null) {
            logger.warn(message + " error:{}", ex.getMessage(), ex);
        }
        if (future != null) {
            try {
                final Object o = future.get();
                logger.info("result :{}", o);
            } catch (Exception futureEx) {
                logger.warn(message + " error:{}", futureEx.getMessage(), futureEx.getCause());
            }
        }
    }

}
