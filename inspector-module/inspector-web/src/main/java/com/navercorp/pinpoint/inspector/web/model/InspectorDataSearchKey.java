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

package com.navercorp.pinpoint.inspector.web.model;

import com.navercorp.pinpoint.metric.common.model.StringPrecondition;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) MetricDataSearchKey과 중복 제거할수 있을듯함.
public class InspectorDataSearchKey {

    private final String metricDefinitionId;

    public InspectorDataSearchKey(String tenantId, String agentId, String metricDefinitionId, TimeWindow timeWindow) {
        this.metricDefinitionId = StringPrecondition.requireHasLength(tenantId, "metricDefinitionId");
    }

    public String getMetricDefinitionId() {
        return metricDefinitionId;
    }
}
