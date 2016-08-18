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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.collector.handler.AgentStatHandler;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.web.dao.stat.SampledActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.SampledCpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.SampledTransactionDao;
import com.navercorp.pinpoint.web.util.TAgentStatBatchBuilder;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.chart.AgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.LegacyAgentStatChartGroup;
import org.junit.Assert;
import org.junit.Before;
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
public class AgentStatChartServiceRegressionTest {

    private static final Random RANDOM = new Random();

    private static final long COLLECT_INTERVAL = 5000L;

    private String agentId = String.valueOf(System.nanoTime());

    @Autowired
    @Qualifier("legacyAgentStatChartV1Service")
    private LegacyAgentStatChartService legacyService;

    @Autowired
    @Qualifier("sampledJvmGcDaoV1")
    private SampledJvmGcDao jvmGcDao;

    private JvmGcChartService jvmGcChartService;

    @Autowired
    @Qualifier("sampledCpuLoadDaoV1")
    private SampledCpuLoadDao cpuLoadDao;

    private CpuLoadChartService cpuLoadChartService;

    @Autowired
    @Qualifier("sampledTransactionDaoV1")
    private SampledTransactionDao transactionDao;

    private TransactionChartService transactionChartService;

    @Autowired
    @Qualifier("sampledActiveTraceDaoV1")
    private SampledActiveTraceDao activeTraceDao;

    private ActiveTraceChartService activeTraceChartService;

    @Autowired
    @Qualifier("agentStatHandlerV1")
    private AgentStatHandler agentStatHandler;

    @Before
    public void setUp() {
        this.jvmGcChartService = new JvmGcChartService(this.jvmGcDao);
        this.cpuLoadChartService = new CpuLoadChartService(this.cpuLoadDao);
        this.transactionChartService = new TransactionChartService(this.transactionDao);
        this.activeTraceChartService = new ActiveTraceChartService(this.activeTraceDao);
    }

    @Test
    public void regressionTest() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .withJvmGc()
                    .withJvmGcDetailed()
                    .withCpuLoad()
                    .withTransaction()
                    .withActiveTrace()
                    .build();
            this.agentStatHandler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        // When
        LegacyAgentStatChartGroup legacyAgentStatChartGroup = legacyService.selectAgentStatList(agentId, timeWindow);
        AgentStatChartGroup jvmGcChartGroup = jvmGcChartService.selectAgentChart(agentId, timeWindow);
        AgentStatChartGroup cpuLoadChartGroup = cpuLoadChartService.selectAgentChart(agentId, timeWindow);
        AgentStatChartGroup transactionChartGroup =  transactionChartService.selectAgentChart(agentId, timeWindow);
        AgentStatChartGroup activeTraceChartGroup = activeTraceChartService.selectAgentChart(agentId, timeWindow);
        verifyChartGroup(legacyAgentStatChartGroup, jvmGcChartGroup);
        verifyChartGroup(legacyAgentStatChartGroup, cpuLoadChartGroup);
        verifyChartGroup(legacyAgentStatChartGroup, transactionChartGroup);
        verifyChartGroup(legacyAgentStatChartGroup, activeTraceChartGroup);
    }

    private void verifyChartGroup(LegacyAgentStatChartGroup legacyAgentStatChartGroup, AgentStatChartGroup agentStatChartGroup) {
        Map<AgentStatChartGroup.ChartType, Chart> legacyCharts = legacyAgentStatChartGroup.getCharts();
        Map<AgentStatChartGroup.ChartType, Chart> charts = agentStatChartGroup.getCharts();
        for (Map.Entry<AgentStatChartGroup.ChartType, Chart> entry : charts.entrySet()) {
            AgentStatChartGroup.ChartType chartType = entry.getKey();
            Chart chart = entry.getValue();
            Chart legacyChart = legacyCharts.get(LegacyAgentStatChartGroup.AgentStatChartType.valueOf(chartType.toString()));
            Assert.assertEquals(chartType + " chart different", chart, legacyChart);
        }
    }
}
