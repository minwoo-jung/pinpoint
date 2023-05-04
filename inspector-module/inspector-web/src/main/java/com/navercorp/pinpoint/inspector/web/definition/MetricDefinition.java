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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
//TODO : (minwoo) com.navercorp.pinpoint.metric.web.mapping.Metric 통합 필요
public class MetricDefinition {


    private final String definitionId;
    private final String metricName;
    private final String title;
    private final List<Field> fields;

    @JsonCreator
    public MetricDefinition(@JsonProperty("definitionId") String definitionId,
                            @JsonProperty("metricName") String metricName,
                            @JsonProperty("title") String title,
                            @JsonProperty("fields") List<Field> fields) {
        this.definitionId = Objects.requireNonNull(definitionId, "definitionId");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.title = Objects.requireNonNull(title, "title");
        this.fields = Objects.requireNonNull(fields, "fields");
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getTitle() {
        return title;
    }

    public List<Field> getFields() {
        return fields;
    }
}
