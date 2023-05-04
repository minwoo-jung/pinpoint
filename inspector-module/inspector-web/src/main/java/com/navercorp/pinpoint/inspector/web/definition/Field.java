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
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class Field {

    private final String fieldName;
    private final List<Tag> tags;
    private final MatchingRule matchingRule;
    private final AggregationFunction aggregationFunction;
    private final String postProcess;

    @JsonCreator
    public Field(@JsonProperty("fieldName") String fieldName,
                 @JsonProperty("tags") List<Tag> tags,
                 @JsonProperty("matchingRule") MatchingRule matchingRule,
                 @JsonProperty("aggregationFunction") AggregationFunction aggregationFunction,
                 @JsonProperty("postProcess") String postProcess){
        this.fieldName = Objects.requireNonNull(fieldName, "name");
        this.tags = defaultTags(tags);
        this.matchingRule = Objects.requireNonNull(matchingRule, "matchingRule");
        this.aggregationFunction = Objects.requireNonNull(aggregationFunction, "aggregationFunction");
        this.postProcess = postProcess;
    }

    private List<Tag> defaultTags(List<Tag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return tags;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public MatchingRule getMatchingRule() {
        return matchingRule;
    }

    public AggregationFunction getAggregationFunction() {
        return aggregationFunction;
    }

    public String getPostProcess() {
        return postProcess;
    }
}
