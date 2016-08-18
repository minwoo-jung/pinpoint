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

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.collector.handler.AgentStatHandler;
import com.navercorp.pinpoint.common.server.bo.ActiveTraceHistogramBo;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TTransaction;
import com.navercorp.pinpoint.web.util.TAgentStatBatchBuilder;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class LegacyHbaseAgentStatDaoTest {

    private static final Random RANDOM = new Random();

    private static final long COLLECT_INTERVAL = 5000L;

    private static final double DECIMAL_COMPARISON_DELTA = 1 / Math.pow(10, AgentStatUtils.NUM_DECIMALS);

    private String agentId;

    @Autowired
    private LegacyHbaseAgentStatDao agentStatDao;

    @Autowired
    @Qualifier("agentStatHandlerV1")
    private AgentStatHandler agentStatHandler;

    @Before
    public void setUp() {
        this.agentId = String.valueOf(System.nanoTime());
    }

    @Test
    public void testGetAgentStatList() {
        // Given
        List<TAgentStat> tAgentStats = new ArrayList<>();
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
            tAgentStats.addAll(agentStatBatch.getAgentStats());
            this.agentStatHandler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        // When
        List<AgentStat> agentStats = this.agentStatDao.getAgentStatList(this.agentId, range);
        // Then
        verifyAgentStats(tAgentStats, agentStats);
    }

    private void verifyAgentStats(List<TAgentStat> tAgentStats, List<AgentStat> agentStats) {
        Assert.assertEquals("agentStat sizes different.", tAgentStats.size(), agentStats.size());
        for (int i = 0; i < tAgentStats.size(); ++i) {
            // AgentStats are scanned in descending order (timestamp)
            TAgentStat tAgentStat = tAgentStats.get((tAgentStats.size() - i) - 1);
            AgentStat agentStat = agentStats.get(i);
            Assert.assertEquals("agentId different", tAgentStat.getAgentId(), agentStat.getAgentId());
            Assert.assertEquals("timestamp different", tAgentStat.getTimestamp(), agentStat.getTimestamp());
            verifyJvmGc(tAgentStat.getGc(), agentStat);
            verifyCpuLoad(tAgentStat.getCpuLoad(), agentStat);
            verifyTransaction(tAgentStat.getTransaction(), agentStat);
            verifyActiveTrace(tAgentStat.getActiveTrace(), agentStat);
        }
    }

    private void verifyJvmGc(TJvmGc jvmGc, AgentStat agentStat) {
        if (jvmGc == null) {
            Assert.assertEquals("heapUsed different", AgentStat.NOT_COLLECTED, agentStat.getHeapUsed());
            Assert.assertEquals("heapMax different", AgentStat.NOT_COLLECTED, agentStat.getHeapMax());
            Assert.assertEquals("nonHeapUsed different", AgentStat.NOT_COLLECTED, agentStat.getNonHeapUsed());
            Assert.assertEquals("nonHeapMax different", AgentStat.NOT_COLLECTED, agentStat.getNonHeapMax());
            Assert.assertEquals("gcOldCount different", AgentStat.NOT_COLLECTED, agentStat.getGcOldCount());
            Assert.assertEquals("gcOldTime different", AgentStat.NOT_COLLECTED, agentStat.getGcOldTime());
        } else {
            Assert.assertEquals("heapUsed different", jvmGc.getJvmMemoryHeapUsed(), agentStat.getHeapUsed());
            Assert.assertEquals("heapMax different", jvmGc.getJvmMemoryHeapMax(), agentStat.getHeapMax());
            Assert.assertEquals("nonHeapUsed different", jvmGc.getJvmMemoryNonHeapUsed(), agentStat.getNonHeapUsed());
            Assert.assertEquals("nonHeapMax different", jvmGc.getJvmMemoryNonHeapMax(), agentStat.getNonHeapMax());
            Assert.assertEquals("gcOldCount different", jvmGc.getJvmGcOldCount(), agentStat.getGcOldCount());
            Assert.assertEquals("gcOldTime different", jvmGc.getJvmGcOldTime(), agentStat.getGcOldTime());
        }
    }

    private void verifyCpuLoad(TCpuLoad cpuLoad, AgentStat agentStat) {
        if (cpuLoad == null) {
            Assert.assertEquals("jvmCpuLoad different", AgentStat.NOT_COLLECTED, agentStat.getJvmCpuUsage(), DECIMAL_COMPARISON_DELTA);
            Assert.assertEquals("systemCpuLoad different", AgentStat.NOT_COLLECTED, agentStat.getSystemCpuUsage(), DECIMAL_COMPARISON_DELTA);
        } else {
            Assert.assertEquals("jvmCpuLoad different", cpuLoad.getJvmCpuLoad(), agentStat.getJvmCpuUsage(), DECIMAL_COMPARISON_DELTA);
            Assert.assertEquals("systemCpuLoad different", cpuLoad.getSystemCpuLoad(), agentStat.getSystemCpuUsage(), DECIMAL_COMPARISON_DELTA);
        }
    }

    private void verifyTransaction(TTransaction transaction, AgentStat agentStat) {
        if (transaction == null) {
            Assert.assertEquals("sampledNew different", AgentStat.NOT_COLLECTED, agentStat.getSampledNewCount());
            Assert.assertEquals("sampledContinuation different", AgentStat.NOT_COLLECTED, agentStat.getSampledContinuationCount());
            Assert.assertEquals("unsampledNew different", AgentStat.NOT_COLLECTED, agentStat.getUnsampledNewCount());
            Assert.assertEquals("unsampledContinuation different", AgentStat.NOT_COLLECTED, agentStat.getUnsampledContinuationCount());
        } else {
            Assert.assertEquals("sampledNew different", transaction.getSampledNewCount(), agentStat.getSampledNewCount());
            Assert.assertEquals("sampledContinuation different", transaction.getSampledContinuationCount(), agentStat.getSampledContinuationCount());
            Assert.assertEquals("unsampledNew different", transaction.getUnsampledNewCount(), agentStat.getUnsampledNewCount());
            Assert.assertEquals("unsampledContinuation different", transaction.getUnsampledContinuationCount(), agentStat.getUnsampledContinuationCount());
        }
    }

    private void verifyActiveTrace(TActiveTrace activeTrace, AgentStat agentStat) {
        if (activeTrace == null) {
            Assert.assertNull("histogramSchema should be null", agentStat.getHistogramSchema());
            Assert.assertNull("activeTraceCounts should be null", agentStat.getActiveTraceCounts());
        } else {
            TActiveTraceHistogram histogram = activeTrace.getHistogram();
            if (histogram == null) {
                Assert.assertNull("histogramSchema should be null", agentStat.getHistogramSchema());
                Assert.assertNull("activeTraceCounts should be null", agentStat.getActiveTraceCounts());
            } else {
                ActiveTraceHistogramBo activeTraceHistogramBo = new ActiveTraceHistogramBo(
                        histogram.getVersion(), histogram.getHistogramSchemaType(), histogram.getActiveTraceCount());
                Assert.assertEquals("histogramSchema different",
                        BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(activeTraceHistogramBo.getHistogramSchemaType()),
                        agentStat.getHistogramSchema());
                Assert.assertEquals("activeTraceCounts different",
                        activeTraceHistogramBo.getActiveTraceCountMap(),
                        agentStat.getActiveTraceCounts());
            }
        }
    }
}
