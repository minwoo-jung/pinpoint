/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.security.util;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.FutureListener;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ExpiredTaskManager<K> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ConcurrentHashMap<K, DefaultFuture<Boolean>> futureMap = new ConcurrentHashMap<K, DefaultFuture<Boolean>>();

    private final Timer timer;
    private final long defaultExpiryTime;

    public ExpiredTaskManager(Timer timer, long defaultExpiryTime) {
        this.timer = Assert.requireNonNull(timer, "timer");

        Assert.isTrue(defaultExpiryTime > 0, "defaultExpiryTime must be greater than zero");
        this.defaultExpiryTime = defaultExpiryTime;
    }

    public boolean reserve(K key, FutureListener<Boolean> futureListener) {
        return reserve(key, defaultExpiryTime, futureListener);
    }

    public boolean reserve(K key, long expiryTime, FutureListener<Boolean> futureListener) {
        Assert.requireNonNull(key, "key");
        Assert.isTrue(expiryTime > 0, "expiryTime must be greater than zero");
        Assert.requireNonNull(futureListener, "futureListener");

        if (isDebug) {
            logger.debug("reserve() started. key:{}", key);
        }

        DefaultFuture<Boolean> future = new DefaultFuture<Boolean>(expiryTime);
        DefaultFuture<Boolean> old = futureMap.putIfAbsent(key, future);
        if (old != null) {
            logger.warn("future already exist(key:{})", key);
            return false;
        }

        future.setListener(futureListener);
        Timeout timeout = timer.newTimeout(future, expiryTime, TimeUnit.MILLISECONDS);
        future.setTimeout(timeout);
        return true;
    }

    public boolean succeed(K key) {
        return done(key, true);
    }

    public boolean failed(K key) {
        return done(key, false);
    }

    private boolean done(K key, boolean result) {
        DefaultFuture<Boolean> future = futureMap.remove(key);
        if (future != null) {
            future.setResult(result);
            return true;
        }
        logger.warn("can't find future(key:{})", key);
        return false;
    }

}
