/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.collector.interceptor;

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public class SpanStatData {

    private final AtomicLongMap<SpanStatKey> statData = AtomicLongMap.create();


    public void incrementSpanCount(SpanStatKey spanStatKey) {
        statData.incrementAndGet(spanStatKey);
    }

    public Map<SpanStatKey, Long> getSnapshotAndRemoveSpanStatData() {
        return AtomicLongMapUtils.remove(statData);
    }

    public static class SpanStatKey {
        private final String organization;
        private final String applicationId;
        private final String agentId;
        private final long spanTime;

        public SpanStatKey(String organization, String applicationId, String agentId, long spanTime) {
            if (StringUtils.isEmpty(organization)) {
                throw new IllegalArgumentException("organization must not be empty");
            }
            if (StringUtils.isEmpty(applicationId)) {
                throw new IllegalArgumentException("applicationId must not be empty");
            }
            if (StringUtils.isEmpty(agentId)) {
                throw new IllegalArgumentException("agentId must not be empty");
            }

            this.organization = organization;
            this.applicationId = applicationId;
            this.agentId = agentId;
            this.spanTime= spanTime;
        }

        public String getOrganization() {
            return organization;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public long getSpanTime() {
            return spanTime;
        }

        public String getAgentId() {
            return agentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SpanStatKey that = (SpanStatKey) o;

            if (spanTime != that.spanTime) return false;
            if (!organization.equals(that.organization)) return false;
            if (!applicationId.equals(that.applicationId)) return false;
            return agentId.equals(that.agentId);
        }

        @Override
        public int hashCode() {
            int result = organization.hashCode();
            result = 31 * result + applicationId.hashCode();
            result = 31 * result + agentId.hashCode();
            result = 31 * result + (int) (spanTime ^ (spanTime >>> 32));
            return result;
        }

    }
}
