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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.web.dao.hbase.stat.LegacyHbaseAgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.ActiveTraceDao;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import com.navercorp.pinpoint.web.util.TAgentStatBatchBuilder;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentStatDaoRegressionTest {

    private static final Random RANDOM = new Random();

    private static final long COLLECT_INTERVAL = 5000L;

    private static final double DECIMAL_COMPARISON_DELTA = 1 / Math.pow(10, AgentStatUtils.NUM_DECIMALS);

    private String agentId = String.valueOf(System.nanoTime());

    @Autowired
    private LegacyHbaseAgentStatDao legacyAgentStatDao;

    @Autowired
    @Qualifier("jvmGcDaoV1")
    private JvmGcDao jvmGcDao;

    @Autowired
    @Qualifier("jvmGcDetailedDaoV1")
    private JvmGcDetailedDao jvmGcDetailedDao;

    @Autowired
    @Qualifier("cpuLoadDaoV1")
    private CpuLoadDao cpuLoadDao;

    @Autowired
    @Qualifier("transactionDaoV1")
    private TransactionDao transactionDao;

    @Autowired
    @Qualifier("activeTraceDaoV1")
    private ActiveTraceDao activeTraceDao;

    @Autowired
    @Qualifier("agentStatHandlerV1")
    private AgentStatHandler agentStatHandler;

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
        // When
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        List<AgentStat> agentStats = legacyAgentStatDao.getAgentStatList(this.agentId, range);
        List<JvmGcBo> jvmGcBos = jvmGcDao.getAgentStatList(this.agentId, range);
        List<JvmGcDetailedBo> jvmGcDetailedBos = jvmGcDetailedDao.getAgentStatList(this.agentId, range);
        List<CpuLoadBo> cpuLoadBos = cpuLoadDao.getAgentStatList(this.agentId, range);
        List<TransactionBo> transactionBos = transactionDao.getAgentStatList(this.agentId, range);
        List<ActiveTraceBo> activeTraceBos = activeTraceDao.getAgentStatList(this.agentId, range);
        // Then
        verifyJvmGc(agentStats, jvmGcBos);
        verifyJvmGcDetailed(agentStats, jvmGcDetailedBos);
        verifyCpuLoad(agentStats, cpuLoadBos);
        verifyTransaction(agentStats, transactionBos);
        verifyActiveTrace(agentStats, activeTraceBos);
    }

    @Test
    public void regressionTest_empty() {
        // Given
        int numBatches = RANDOM.nextInt(10) + 1;
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = initialTimestamp;
        for (int i = 0; i < numBatches; ++i) {
            int numStats = RANDOM.nextInt(10) + 1;
            TAgentStatBatch agentStatBatch = new TAgentStatBatchBuilder(this.agentId, timestamp, COLLECT_INTERVAL, numStats)
                    .build();
            this.agentStatHandler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        // When
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        List<AgentStat> agentStats = legacyAgentStatDao.getAgentStatList(this.agentId, range);
        List<JvmGcBo> jvmGcBos = jvmGcDao.getAgentStatList(this.agentId, range);
        List<JvmGcDetailedBo> jvmGcDetailedBos = jvmGcDetailedDao.getAgentStatList(this.agentId, range);
        List<CpuLoadBo> cpuLoadBos = cpuLoadDao.getAgentStatList(this.agentId, range);
        List<TransactionBo> transactionBos = transactionDao.getAgentStatList(this.agentId, range);
        List<ActiveTraceBo> activeTraceBos = activeTraceDao.getAgentStatList(this.agentId, range);
        // Then
        verifyJvmGc(agentStats, jvmGcBos);
        verifyJvmGcDetailed(agentStats, jvmGcDetailedBos);
        verifyCpuLoad(agentStats, cpuLoadBos);
        verifyTransaction(agentStats, transactionBos);
        verifyActiveTrace(agentStats, activeTraceBos);
    }

    private void verifyJvmGc(List<AgentStat> agentStats, List<JvmGcBo> jvmGcBos) {
        Assert.assertEquals("jvmGcBos size different", agentStats.size(), jvmGcBos.size());
        for (int i = 0; i < agentStats.size(); ++i) {
            AgentStat agentStat = agentStats.get(i);
            JvmGcBo jvmGcBo = jvmGcBos.get(i);
            verifyCommonFields(agentStat, jvmGcBo);
            Assert.assertEquals("heapUsed different", agentStat.getHeapUsed(), jvmGcBo.getHeapUsed());
            Assert.assertEquals("heapMax different", agentStat.getHeapMax(), jvmGcBo.getHeapMax());
            Assert.assertEquals("nonHeapUsed different", agentStat.getNonHeapUsed(), jvmGcBo.getNonHeapUsed());
            Assert.assertEquals("nonHeapMax different", agentStat.getNonHeapMax(), jvmGcBo.getNonHeapMax());
            Assert.assertEquals("gcOldCount different", agentStat.getGcOldCount(), jvmGcBo.getGcOldCount());
            Assert.assertEquals("gcOldTime different", agentStat.getGcOldTime(), jvmGcBo.getGcOldTime());
        }
    }

    private void verifyJvmGcDetailed(List<AgentStat> agentStats, List<JvmGcDetailedBo> jvmGcDetailedBos) {
        // v1 does not support jvmGcDetailed stat collection
        Assert.assertTrue("jvmGcDetailedBos should be empty", jvmGcDetailedBos.size() == 0);
    }

    private void verifyCpuLoad(List<AgentStat> agentStats, List<CpuLoadBo> cpuLoadBos) {
        Assert.assertEquals("cpuLoadBos size different", agentStats.size(), cpuLoadBos.size());
        for (int i = 0; i < agentStats.size(); ++i) {
            AgentStat agentStat = agentStats.get(i);
            CpuLoadBo cpuLoadBo = cpuLoadBos.get(i);
            verifyCommonFields(agentStat, cpuLoadBo);
            Assert.assertEquals("jvmCpuLoad different", agentStat.getJvmCpuUsage(), cpuLoadBo.getJvmCpuLoad(), DECIMAL_COMPARISON_DELTA);
            Assert.assertEquals("systemCpuLoad different", agentStat.getSystemCpuUsage(), cpuLoadBo.getSystemCpuLoad(), DECIMAL_COMPARISON_DELTA);
        }
    }

    private void verifyTransaction(List<AgentStat> agentStats, List<TransactionBo> transactionBos) {
        Assert.assertEquals("transactionBos size different", agentStats.size(), transactionBos.size());
        for (int i = 0; i < agentStats.size(); ++i) {
            AgentStat agentStat = agentStats.get(i);
            TransactionBo transactionBo = transactionBos.get(i);
            verifyCommonFields(agentStat, transactionBo);
            Assert.assertEquals("sampledNew different", agentStat.getSampledNewCount(), transactionBo.getSampledNewCount());
            Assert.assertEquals("sampledContinuation different", agentStat.getSampledContinuationCount(), transactionBo.getSampledContinuationCount());
            Assert.assertEquals("unsampledNew different", agentStat.getUnsampledNewCount(), transactionBo.getUnsampledNewCount());
            Assert.assertEquals("unsampledContinuation different", agentStat.getUnsampledContinuationCount(), transactionBo.getUnsampledContinuationCount());
        }
    }

    private void verifyActiveTrace(List<AgentStat> agentStats, List<ActiveTraceBo> activeTraceBos) {
        Assert.assertEquals("activeTraceBos size different", agentStats.size(), activeTraceBos.size());
        for (int i = 0; i < agentStats.size(); ++i) {
            AgentStat agentStat = agentStats.get(i);
            ActiveTraceBo activeTraceBo = activeTraceBos.get(i);
            verifyCommonFields(agentStat, activeTraceBo);
            Assert.assertEquals("histogramSchema different", agentStat.getHistogramSchema(), BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(activeTraceBo.getHistogramSchemaType()));
            Assert.assertEquals("activeTraceCounts different", agentStat.getActiveTraceCounts(), activeTraceBo.getActiveTraceCounts());
        }

    }

    private void verifyCommonFields(AgentStat agentStat, AgentStatDataPoint agentStatDataPoint) {
        Assert.assertEquals("agentId different", agentStat.getAgentId(), agentStatDataPoint.getAgentId());
        Assert.assertEquals("timestamp different", agentStat.getTimestamp(), agentStatDataPoint.getTimestamp());
    }
}
