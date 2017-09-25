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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcDetailed;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public class TAgentStatBatchBuilder {

    private static final Random RANDOM = new Random();

    private final String agentId;
    private final long initialTimestamp;
    private final long collectInterval;
    private final int numStats;

    private boolean withJvmGc;
    private boolean withJvmGcDetailed;
    private boolean withCpuLoad;
    private boolean withTransaction;
    private boolean withActiveTrace;

    public TAgentStatBatchBuilder(String agentId, long initialTimestamp, long collectInterval, int numStats) {
        this.agentId = agentId;
        this.initialTimestamp = initialTimestamp;
        this.collectInterval = collectInterval;
        this.numStats = numStats;
    }

    public TAgentStatBatchBuilder withJvmGc() {
        this.withJvmGc = true;
        return this;
    }

    public TAgentStatBatchBuilder withJvmGcDetailed() {
        this.withJvmGcDetailed = true;
        return this;
    }

    public TAgentStatBatchBuilder withCpuLoad() {
        this.withCpuLoad = true;
        return this;
    }

    public TAgentStatBatchBuilder withTransaction() {
        this.withTransaction = true;
        return this;
    }

    public TAgentStatBatchBuilder withActiveTrace() {
        this.withActiveTrace = true;
        return this;
    }

    public TAgentStatBatch build() {
        TAgentStatBatch agentStatBatch = new TAgentStatBatch();
        agentStatBatch.setAgentId(this.agentId);
        List<TAgentStat> agentStats = new ArrayList<>(this.numStats);
        long timestamp = this.initialTimestamp;
        for (int i = 0; i < this.numStats; i++) {
            TAgentStat agentStat = new TAgentStat();
            agentStat.setAgentId(this.agentId);
            agentStat.setTimestamp(timestamp + (collectInterval * i));
            agentStat.setCollectInterval(this.collectInterval);
            if (this.withJvmGc) {
                agentStat.setGc(createJvmGc());
            }
            if (this.withJvmGcDetailed) {
                if (!agentStat.isSetGc()) {
                    agentStat.setGc(createJvmGc());
                }
                agentStat.getGc().setJvmGcDetailed(createJvmGcDetailed());
            }
            if (this.withCpuLoad) {
                agentStat.setCpuLoad(createCpuLoad());
            }
            if (this.withTransaction) {
                agentStat.setTransaction(createTransaction());
            }
            if (this.withActiveTrace) {
                agentStat.setActiveTrace(createActiveTrace());
            }
            agentStats.add(agentStat);
        }
        agentStatBatch.setAgentStats(agentStats);
        return agentStatBatch;
    }

    private static TJvmGc createJvmGc() {
        TJvmGc jvmGc = new TJvmGc();
        jvmGc.setType(TJvmGcType.CMS);
        jvmGc.setJvmMemoryHeapUsed(getUnsignedRandomLong());
        jvmGc.setJvmMemoryHeapMax(getUnsignedRandomLong());
        jvmGc.setJvmMemoryNonHeapUsed(getUnsignedRandomLong());
        jvmGc.setJvmMemoryNonHeapMax(getUnsignedRandomLong());
        jvmGc.setJvmGcOldCount(getUnsignedRandomLong());
        jvmGc.setJvmGcOldTime(getUnsignedRandomLong());
        return jvmGc;
    }

    private static TJvmGcDetailed createJvmGcDetailed() {
        TJvmGcDetailed jvmGcDetailed = new TJvmGcDetailed();
        jvmGcDetailed.setJvmGcNewCount(getUnsignedRandomLong());
        jvmGcDetailed.setJvmGcNewTime(getUnsignedRandomLong());
        jvmGcDetailed.setJvmPoolCodeCacheUsed(getRandomDouble());
        jvmGcDetailed.setJvmPoolNewGenUsed(getRandomDouble());
        jvmGcDetailed.setJvmPoolOldGenUsed(getRandomDouble());
        jvmGcDetailed.setJvmPoolSurvivorSpaceUsed(getRandomDouble());
        jvmGcDetailed.setJvmPoolPermGenUsed(getRandomDouble());
        jvmGcDetailed.setJvmPoolMetaspaceUsed(getRandomDouble());
        return jvmGcDetailed;
    }

    private static TCpuLoad createCpuLoad() {
        TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(getRandomDouble());
        cpuLoad.setSystemCpuLoad(getRandomDouble());
        return cpuLoad;
    }

    private static TTransaction createTransaction() {
        TTransaction transaction = new TTransaction();
        transaction.setSampledNewCount(RANDOM.nextInt(10000000));
        transaction.setSampledContinuationCount(RANDOM.nextInt(10000000));
        transaction.setUnsampledNewCount(RANDOM.nextInt(10000000));
        transaction.setUnsampledContinuationCount(RANDOM.nextInt(10000000));
        return transaction;
    }

    private static TActiveTrace createActiveTrace() {
        TActiveTraceHistogram activeTraceHistogram = new TActiveTraceHistogram();
        activeTraceHistogram.setHistogramSchemaType(BaseHistogramSchema.NORMAL_SCHEMA.getTypeCode());
        activeTraceHistogram.setActiveTraceCount(Arrays.asList(
                getUnsignedRandomInt(), getUnsignedRandomInt(), getUnsignedRandomInt(), getUnsignedRandomInt()
        ));
        TActiveTrace activeTrace = new TActiveTrace();
        activeTrace.setHistogram(activeTraceHistogram);
        return activeTrace;
    }

    private static int getUnsignedRandomInt() {
        return Math.abs(RANDOM.nextInt());
    }

    private static long getUnsignedRandomLong() {
        return Math.abs(RANDOM.nextLong());
    }

    private static double getRandomDouble() {
        return RANDOM.nextDouble();
    }
}
