package com.navercorp.pinpoint.testweb.service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.springframework.stereotype.Service;

@Service("memcachedService")
public class MemcachedServiceImpl implements MemcachedService {
    private static final String KEY = "pinpoint:testkey";

    private MemcachedClient memcached;

    @PostConstruct
    public void init() throws Exception {
        memcached = new MemcachedClient(AddrUtil.getAddresses("10.99.200.15:11316,10.99.200.16:11316,10.99.200.17:11316"));
    }

    @PreDestroy
    public void destroy() {
        memcached.shutdown();
    }

    public void set() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = memcached.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (setFuture != null)
                setFuture.cancel(true);
        }
    }

    public void get() {
        Future<Object> getFuture = null;
        try {
            getFuture = memcached.asyncGet(KEY);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (getFuture != null)
                getFuture.cancel(true);
        }
    }

    public void delete() {
        Future<Boolean> delFuture = null;
        try {
            delFuture = memcached.delete(KEY);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (delFuture != null)
                delFuture.cancel(true);
        }
    }

    public void timeout() {
        Future<Boolean> setFuture = null;
        try {
            setFuture = memcached.set(KEY, 10, "Hello, pinpoint.");
            setFuture.get(1L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (setFuture != null)
                setFuture.cancel(true);
        }
    }
}
