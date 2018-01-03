/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatMetricRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author minwoo.jung
 */
public class DefaultUriStatMetricRegistry implements UriStatMetricRegistry {

    private Map<String, UriStatMetric> uriStatMetricMap = new ConcurrentHashMap<String, UriStatMetric>();

    @Override
    public void incrementUriCount(String uri) {
        UriStatMetric uriStatMetric = uriStatMetricMap.get(uri);

        if (uriStatMetric == null) {
            uriStatMetric = new UriStatMetric(uri);
            uriStatMetricMap.put(uri, uriStatMetric);
        }

        uriStatMetric.incrementCount();
    }

    public class UriStatMetric {
        private final String uri;
        private volatile AtomicLong count;

        public UriStatMetric(String uri) {
            this.uri = uri;
            count = new AtomicLong(0);
        }

        public void incrementCount() {
            count.incrementAndGet();
        }
    }
}
