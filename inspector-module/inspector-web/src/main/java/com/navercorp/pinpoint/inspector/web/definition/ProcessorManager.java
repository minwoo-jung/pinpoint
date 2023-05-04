/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.definition;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 *
 */
@Component
public class ProcessorManager {
    private Map<String, PostProcessor> postProcessorMap = new HashMap<>();

    public ProcessorManager(List<PostProcessor> postProcessorList) {
        for (PostProcessor postProcessor : postProcessorList) {
            postProcessorMap.put(postProcessor.getName(), postProcessor);
        }
    }

    public PostProcessor getPostProcessor(String name) {
        if (StringUtils.isEmpty(name)) {
            return EmptyPostProcessor.INSTANCE;
        }
        if (!postProcessorMap.containsKey(name)) {
            throw new IllegalArgumentException("postProcessor not found. name:" + name);
        }

        return postProcessorMap.get(name);
    }

}
