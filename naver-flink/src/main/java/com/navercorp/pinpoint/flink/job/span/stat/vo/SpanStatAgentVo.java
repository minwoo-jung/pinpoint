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

import com.navercorp.pinpoint.common.util.DateUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author minwoo.jung
 */
public class SpanStatAgentVo {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String organization;
    private String applicationId;
    private String agentId;
    private long count;
    private long timestamp;

    public SpanStatAgentVo() {
    }

    public SpanStatAgentVo(SpanStatAgentKey spanStatAgentKey, long count, long timestamp) {
        this(spanStatAgentKey.getOrganization(), spanStatAgentKey.getApplicationId(), spanStatAgentKey.getAgentId(), count, timestamp);
    }

    public SpanStatAgentVo(String organization, String applicationId, String agentId, long count, long timestamp) {
        if (StringUtils.isEmpty(organization)) {
            throw new IllegalArgumentException("organization must not be empty.");
        }
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalArgumentException("applicationId must not be empty.");
        }
        if (StringUtils.isEmpty(agentId)) {
            throw new IllegalArgumentException("agentId must not be empty.");
        }
        this.organization = organization;
        this.applicationId = applicationId;
        this.agentId = agentId;
        this.count = count;
        this.timestamp = timestamp;
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

    public long getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
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

    public void setCount(long count) {
        this.count = count;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return DateUtils.longToDateStr(timestamp, DATE_TIME_FORMAT);
    }

    public void setDateTime(String dateTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date parsedDate = format.parse(dateTime);

        this.timestamp = parsedDate.getTime();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpanStatAgentVo that = (SpanStatAgentVo) o;

        if (count != that.count) return false;
        if (timestamp != that.timestamp) return false;
        if (!organization.equals(that.organization)) return false;
        if (!applicationId.equals(that.applicationId)) return false;
        return agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = organization.hashCode();
        result = 31 * result + applicationId.hashCode();
        result = 31 * result + agentId.hashCode();
        result = 31 * result + (int) (count ^ (count >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanStatAgentVo{");
        sb.append("organization='").append(organization).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", count=").append(count);
        sb.append(", timeStamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
