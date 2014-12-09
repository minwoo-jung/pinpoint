package com.navercorp.pinpoint.profiler.modifier.redis.filter;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.modifier.redis.filter.JedisPipelineMethodNames;

public class JedisPipelineMethodNamesTest {

    @Test
    public void test() {
        Set<String> names = JedisPipelineMethodNames.get();

        assertTrue(names.contains("get"));
        assertTrue(names.contains("sinterstore"));
        assertTrue(names.contains("info"));
        assertTrue(names.contains("sync"));
        assertTrue(names.contains("syncAndReturnAll"));
    }
}
