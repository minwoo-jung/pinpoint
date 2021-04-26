/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.model;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricTagCollection {

    //TODO : (minwoo) applicationId 이름을 다른 이름으로 써야할듯함.
    private final String applicationId;
    private final String hostName;
    private final String metricName;
    private final String fieldName;

    private final List<MetricTag> metricTagList;

    public MetricTagCollection(String applicationId, String hostName, String metricName, String fieldName, List<MetricTag> metricTagList) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalArgumentException("applicationId must not be empty");
        }
        if (StringUtils.isEmpty(hostName)) {
            throw new IllegalArgumentException("hostName must not be empty");
        }
        if (StringUtils.isEmpty(metricName)) {
            throw new IllegalArgumentException("metricName must not be empty");
        }
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName must not be empty");
        }

        this.applicationId = applicationId;
        this.hostName = hostName;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.metricTagList = Objects.requireNonNull(metricTagList, "metricTagList");
    }

    public List<MetricTag> getMetricTagList() {
        return metricTagList;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return "MetricTagCollection{" +
                "applicationId='" + applicationId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", metricTagList=" + metricTagList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricTagCollection that = (MetricTagCollection) o;
        return Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(hostName, that.hostName) &&
                Objects.equals(metricName, that.metricName) &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(metricTagList, that.metricTagList);
    }
}
