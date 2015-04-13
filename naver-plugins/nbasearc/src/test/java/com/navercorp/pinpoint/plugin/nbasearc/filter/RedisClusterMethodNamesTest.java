/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nbasearc.filter;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterMethodNamesTest {

    @Test
    public void test() {
        Set<String> names = RedisClusterMethodNames.get();

        assertTrue(names.contains("get"));
        assertTrue(names.contains("sinterstore"));
        assertTrue(names.contains("info"));

        assertTrue(names.contains("zadd2"));
        assertTrue(names.contains("slexpire"));
        assertTrue(names.contains("ssttl"));
    }

}
