package com.navercorp.pinpoint.testweb.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;

@Service("arcusService")
public class ArcusServiceImpl implements ArcusService {
    private static final String KEY = "pinpoint:testkey";

    @Autowired
    @Qualifier("arcusClientFactory")
    private ArcusClient arcus;

    public void set() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = arcus.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (setFuture != null)
                setFuture.cancel(true);
        }
    }

    public void get() {
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(KEY);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (getFuture != null)
                getFuture.cancel(true);
        }
    }

    public void delete() {
        Future<Boolean> delFuture = null;
        try {
            delFuture = arcus.delete(KEY);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (delFuture != null)
                delFuture.cancel(true);
        }
    }

    public void timeout() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = arcus.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (setFuture != null)
                setFuture.cancel(true);
        }
    }

    @Override
    public void getAndAsyncCallback() {
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(KEY);
            final Future<Object> future = getFuture;
            final CountDownLatch latch = new CountDownLatch(1);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.get(1000L, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                    }
                    latch.countDown();
                }
            });
            thread.start();
            latch.await(2000L, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            if (getFuture != null)
                getFuture.cancel(true);
        }
    }
}