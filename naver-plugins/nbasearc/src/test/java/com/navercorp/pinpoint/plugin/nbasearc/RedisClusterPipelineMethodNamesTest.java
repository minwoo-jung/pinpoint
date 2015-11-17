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
package com.navercorp.pinpoint.plugin.nbasearc;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.plugin.nbasearc.RedisClusterPipelineMethodNames;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineMethodNamesTest {

    @Test
    public void test() {
        List<String> names = Arrays.asList(RedisClusterPipelineMethodNames.get());

        assertTrue(names.contains("get"));
        assertTrue(names.contains("sinterstore"));
        assertTrue(names.contains("info"));
        assertTrue(names.contains("sync"));
        assertTrue(names.contains("syncAndReturnAll"));
        assertTrue(names.contains("close"));
    }
}
