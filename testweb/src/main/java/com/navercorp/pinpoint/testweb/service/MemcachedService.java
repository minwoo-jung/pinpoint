package com.navercorp.pinpoint.testweb.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.OperationFuture;

public interface MemcachedService {

    void set();

    void get();

    void delete();

    void timeout();

    void asyncCAS();

    void asyncGetBulk();

    void getAndTouch();
}
