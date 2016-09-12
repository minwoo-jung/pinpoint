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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("memcachedService")
public class MemcachedServiceImpl implements MemcachedService {
    private static final String KEY = "pinpoint:testkey";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("memcachedClientFactory")
    private MemcachedClient memcached;

    
    public void set() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = memcached.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, setFuture, "set");
        }
    }

    public void get() {
        Future<Object> getFuture = null;
        try {
            getFuture = memcached.asyncGet(KEY);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, getFuture, "get");
        }
    }

    
    
    public void delete() {
        Future<Boolean> delFuture = null;
        try {
            delFuture = memcached.delete(KEY);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, delFuture, "delete");
        }
    }

    public void timeout() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = memcached.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, setFuture, "set");
        }
    }


    public void asyncCAS() {
//        OperationFuture<CASResponse> future = null;
//        try {
//            future = memcached.asyncCAS("test-asyncCAS", 1L, "foo");
//            future.get(1000L, TimeUnit.MILLISECONDS);
//        } catch (Exception e) {
//            if (future != null)
//                future.cancel();
//        }
    }
    

    public void asyncGetBulk() {
//        BulkFuture<Map<String, Object>> future = null;
//        try {
//            future = memcached.asyncGetBulk("test-async-get-bulk");
//            future.get(1000L, TimeUnit.MILLISECONDS);
//        } catch (Exception e) {
//            if (future != null)
//                future.cancel(true);
//        }
    }

    
    public void getAndTouch() {
//        CASValue<Object> future = null;
//        try {
//            future = memcached.getAndTouch("test-get-and-touch", 1);
//            future.getValue();
//        } catch (Exception e) {
//        }
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
