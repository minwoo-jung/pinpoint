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

package com.navercorp.pinpoint.inspector.web.view;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.metric.web.model.MetricValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) SystemMetricView 과 중복 제거
public class InspectorMetricView {
    private final InspectorMetricData<? extends Number> inspectorMetricData;

    public InspectorMetricView(InspectorMetricData<? extends Number> inspectorMetricData) {
        this.inspectorMetricData = Objects.requireNonNull(inspectorMetricData, "inspectorMetricData");
    }

    public String getTitle() {
        return inspectorMetricData.getTitle();
    }

    public List<Long> getTimeStamp() {
        return inspectorMetricData.getTimeStampList();
    }

    public List<MetricValueView> getMetricValues() {
        return inspectorMetricData.getMetricValueList().stream()
                .map(MetricValueView::new)
                .collect(Collectors.toList());
    }

    public static class MetricValueView {
        private final MetricValue metricValue;

        public MetricValueView(MetricValue metricValue) {
            this.metricValue = Objects.requireNonNull(metricValue, "metricValue");
        }

        public MetricValue getMetricValue() {
            return metricValue;
        }
    }
}
