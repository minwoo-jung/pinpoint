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

package com.navercorp.pinpoint.inspector.web.controller;

import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.service.AgentStatService;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.web.view.SystemMetricView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping("/getAgentStatV2/{chartType}")
public class AgentStatController {

    AgentStatService agentStatChartService;

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(10000L, 200);
    private final TenantProvider tenantProvider;

    public AgentStatController(AgentStatService agentStatChartService, TenantProvider tenantProvider) {
        this.agentStatChartService = Objects.requireNonNull(agentStatChartService, "agentStatChartService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    // TODO : (minwoo) tenantId 고려해야함. collector 쪽도 고려해야함.
    @GetMapping(value = "/chart")
    public SystemMetricView getAgentStatChart(
            @RequestParam("agentId") String agentId,
            @PathVariable("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, agentId, metricDefinitionId, timeWindow);

        SystemMetricData<? extends Number> systemMetricData =  agentStatChartService.selectAgentStat(inspectorDataSearchKey);
        return new SystemMetricView(systemMetricData);
    }
}
