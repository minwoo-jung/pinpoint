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
package com.navercorp.pinpoint.flink.job.span.stat.vo;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public class SpanStatAgentKey implements SpanStatVo {
    public final static SpanStatAgentKey EMPTY_SPAN_STAT_AGENT_KEY = new SpanStatAgentKey(UNKNOWN_ORGANIZATION, UNKNOWN_APPLICATION, UNKNOWN_AGENT);

    private String organization;
    private String applicationId;
    private String agentId;

    public SpanStatAgentKey() {
        this.organization = UNKNOWN_ORGANIZATION;
        this.applicationId = UNKNOWN_APPLICATION;
        this.agentId = UNKNOWN_AGENT;
    }

    public SpanStatAgentKey(String organization, String applicationId, String agentId) {
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
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getOrganization() {
        return organization;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getAgentId() {
        return agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpanStatAgentKey that = (SpanStatAgentKey) o;

        if (!organization.equals(that.organization)) return false;
        if (!applicationId.equals(that.applicationId)) return false;
        return agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = organization.hashCode();
        result = 31 * result + applicationId.hashCode();
        result = 31 * result + agentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanStatAgentKey{");
        sb.append("organization='").append(organization).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
