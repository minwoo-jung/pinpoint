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

package com.navercorp.pinpoint.web.dao.hbase.stat.compatibility;

import com.navercorp.pinpoint.collector.handler.AgentStatHandler;
import com.navercorp.pinpoint.collector.handler.AgentStatHandlerV2;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.util.TAgentStatBatchBuilder;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public abstract class HbaseAgentStatDaoCompatibilityTestBase<T extends AgentStatDataPoint> {

    private static final Random RANDOM = new Random();

    private static final long COLLECT_INTERVAL = 5000L;

    private String agentId = String.valueOf(System.nanoTime());

    protected AgentStatDao<T> v1Dao;

    protected AgentStatDao<T> v2Dao;

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
                    .withJvmGcDetailed()
                    .withCpuLoad()
                    .withTransaction()
                    .withActiveTrace()
                    .build();
            this.v1Handler.handle(agentStatBatch);
            this.v2Handler.handle(agentStatBatch);
            timestamp += COLLECT_INTERVAL * numStats;
        }
        // When
        Range range = new Range(initialTimestamp, timestamp - COLLECT_INTERVAL);
        List<T> v1Bos = v1Dao.getAgentStatList(this.agentId, range);
        List<T> v2Bos = v2Dao.getAgentStatList(this.agentId, range);
        // Then
        verifyBos(v1Bos, v2Bos);
    }

    protected void verifyBos(List<T> v1Bos, List<T> v2Bos) {
        Assert.assertEquals("bos sizes different", v1Bos.size(), v2Bos.size());
        for (int i = 0; i < v1Bos.size(); ++i) {
            T v1Bo = v1Bos.get(i);
            T v2Bo = v2Bos.get(i);
            Assert.assertEquals(v1Bo, v2Bo);
        }
    }
}
