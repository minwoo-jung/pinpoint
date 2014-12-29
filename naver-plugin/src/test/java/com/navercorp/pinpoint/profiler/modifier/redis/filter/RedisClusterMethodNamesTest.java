package com.navercorp.pinpoint.profiler.modifier.redis.filter;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.modifier.nbase.arc.filter.RedisClusterMethodNames;

public class RedisClusterMethodNamesTest {

    @Test
    public void get() {
        Set<String> names = RedisClusterMethodNames.get();
        
        
        assertTrue(names.contains("get"));
        assertTrue(names.contains("sinterstore"));
        assertTrue(names.contains("info"));

        assertTrue(names.contains("zadd2"));
        assertTrue(names.contains("slexpire"));
        assertTrue(names.contains("ssttl"));
    }

}
