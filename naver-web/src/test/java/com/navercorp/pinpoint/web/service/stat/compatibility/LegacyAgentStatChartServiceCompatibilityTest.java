/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.service.stat.compatibility;

import com.navercorp.pinpoint.collector.handler.AgentStatHandler;
import com.navercorp.pinpoint.collector.handler.AgentStatHandlerV2;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.web.service.stat.LegacyAgentStatChartService;
import com.navercorp.pinpoint.web.util.TAgentStatBatchBuilder;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.chart.LegacyAgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.AgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class LegacyAgentStatChartServiceCompatibilityTest {

    private static final Random RANDOM = new Random();

    private static final long COLLECT_INTERVAL = 5000L;

    private String agentId = String.valueOf(System.nanoTime());

    @Autowired
    @Qualifier("legacyAgentStatChartV1Service")
    private LegacyAgentStatChartService v1Service;

    @Autowired
    @Qualifier("legacyAgentStatChartV2Service")
    private LegacyAgentStatChartService v2Service;

    @Autowired
    @Qualifier("agentStatHandlerV1")
    private AgentStatHandler v1Handler;

    @Autowired
    @Qualifier("agentStatHandlerV2")
    private AgentStatHandlerV2 v2Handler;

    @Test
    public void testCompatibility() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withJvmGc()
                    .withCpuLoad()
                    .withTransaction()
                    .withActiveTrace()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    @Test
    public void testCompatibility_without_jvmGc() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withCpuLoad()
                    .withTransaction()
                    .withActiveTrace()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    @Test
    public void testCompatibility_without_cpuLoad() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withJvmGc()
                    .withTransaction()
                    .withActiveTrace()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    @Test
    public void testCompatibility_without_transaction() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withJvmGc()
                    .withCpuLoad()
                    .withActiveTrace()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    @Test
    public void testCompatibility_without_activeTrace() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withJvmGc()
                    .withCpuLoad()
                    .withTransaction()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    @Test
    public void testCompatibility_without_everything() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup v1ChartGroup = v1Service.selectAgentStatList(this.agentId, timeWindow);
        LegacyAgentStatChartGroup v2ChartGroup = v2Service.selectAgentStatList(this.agentId, timeWindow);
        // Then
        verifyChartGroups(v1ChartGroup, v2ChartGroup);
    }

    private void verifyChartGroups(LegacyAgentStatChartGroup v1ChartGroup, LegacyAgentStatChartGroup v2ChartGroup) {
        Assert.assertEquals("gcType different", v1ChartGroup.getType(), v2ChartGroup.getType());
        Map<AgentStatChartGroup.ChartType, Chart> v1Charts = v1ChartGroup.getCharts();
        Map<AgentStatChartGroup.ChartType, Chart> v2Charts = v2ChartGroup.getCharts();
        Assert.assertEquals("chart size different.", v1Charts.size(), v2Charts.size());
        for (Map.Entry<AgentStatChartGroup.ChartType, Chart> entry : v2Charts.entrySet()) {
            AgentStatChartGroup.ChartType chartType = entry.getKey();
            Chart v2Chart = entry.getValue();
            Chart v1Chart = v1Charts.get(LegacyAgentStatChartGroup.AgentStatChartType.valueOf(chartType.toString()));
            Assert.assertEquals(chartType + " chart different", v1Chart, v2Chart);
        }
    }

}
