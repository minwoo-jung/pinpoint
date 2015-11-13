package com.navercorp.pinpoint.testweb.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;

@Service("arcusService")
public class ArcusServiceImpl implements ArcusService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            handelException(e, setFuture, "set");
        }
    }

    public void get() {
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(KEY);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, getFuture, "get");
        }
    }

    public void delete() {
        Future<Boolean> delFuture = null;
        try {
            delFuture = arcus.delete(KEY);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, delFuture, "delete");
        }
    }

    public void timeout() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = arcus.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handelException(e, setFuture, "set");
        }
    }

    @Override
    public void getAndAsyncCallback() {
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(KEY);
            final Future<Object> future = getFuture;

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.get(1000L, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        logger.info("another thread get error:{}", e.getMessage(), e);
                    }
                }
            });
            thread.start();
            thread.join(2000L);
        } catch (Exception e) {
            handelException(e, getFuture, "get");
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