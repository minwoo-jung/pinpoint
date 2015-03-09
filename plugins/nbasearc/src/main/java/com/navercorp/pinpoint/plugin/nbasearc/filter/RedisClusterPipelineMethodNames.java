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

import java.util.Arrays;
import java.util.Set;

/**
 * RedisClusterPipeline method names
 *   - RedisCluster method names + pipeline method names
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineMethodNames {

    private static Set<String> names = null;
    
    public static Set<String> get() {
        if(names != null) {
            return names;
        }
        
        final String[] methodNames = { 
                "sync",
                "syncAndReturnAll",
                "close"
        };
        
        final Set<String> redisClusterMethodNames = RedisClusterMethodNames.get();
        redisClusterMethodNames.addAll(Arrays.asList(methodNames));
        names = redisClusterMethodNames;
        
        return names;
    }
}