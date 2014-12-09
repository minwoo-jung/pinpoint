package com.navercorp.pinpoint.profiler.modifier.redis.filter;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.modifier.redis.filter.JedisMethodNames;

public class JedisMethodNamesTest {

    @Test
    public void get() {
        Set<String> names = JedisMethodNames.get();

        assertTrue(names.contains("get"));
        assertTrue(names.contains("sinterstore"));
        assertTrue(names.contains("info"));
    }
}
