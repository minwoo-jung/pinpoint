package com.navercorp.pinpoint.testweb.service;

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

    public static final int RANDOM_RANGE = 1000;
    @Autowired
    @Qualifier("arcusClientFactory")
    private ArcusClient arcus;

    @Autowired
    @Qualifier("memcachedClientFactory")
    private MemcachedClient memcached;

    private final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            logger.warn("delete error:{}", e.getMessage(), e);
            if (getFuture != null) {
                getFuture.cancel(true);
            }
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
                logger.info("delete error:{}", e.getMessage(), e);
                if (delFuture != null) {
                    delFuture.cancel(true);
                }
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
                logger.warn("set error:{}", e.getMessage(), e);
                if (setFuture != null) {
                    setFuture.cancel(true);
                }
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
            logger.warn("set error:{}", e.getMessage(), e);
            if (setFuture != null) {
                setFuture.cancel(true);
            }
        }

        // get
        Future<Object> getFuture = null;
        try {
            getFuture = arcus.asyncGet(key);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("asyncGet error:{}", e.getMessage(), e);
            if (getFuture != null) {
                getFuture.cancel(true);
            }
        }

        // del
        Future<Boolean> delFuture = null;
        try {
            delFuture = arcus.delete(key);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("delete error:{}", e.getMessage(), e);
            if (delFuture != null) {
                delFuture.cancel(true);
            }
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
            logger.warn("set error:{}", e.getMessage(), e);
            if (setFuture != null) {
                setFuture.cancel(true);
            }
        }

        // get
        Future<Object> getFuture = null;
        try {
            getFuture = memcached.asyncGet(key);
            getFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("asyncGet error:{}", e.getMessage(), e);
            if (getFuture != null) {
                getFuture.cancel(true);
            }
        }

        // del
        Future<Boolean> delFuture = null;
        try {
            delFuture = memcached.delete(key);
            delFuture.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.info("delete error:{}", e.getMessage(), e);
            if (delFuture != null) {
                delFuture.cancel(true);
            }
        }
    }


}
