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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.mapper.RangeTimestampFilter;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 * @author Minwoo Jung
 */
@Repository
public class HbaseApplicationStatDaoOperationsV2 {

    private static final int APPLICATION_STAT_NUM_PARTITIONS = 32;
    private static final int MAX_SCAN_CACHE_SIZE = 256;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private ApplicationStatHbaseOperationFactory operationFactory;

    <T extends AgentStatDataPoint> List<T> getAgentStatList(StatType statType, AgentStatMapperV2<T> mapper, String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        Scan scan = this.createScan(statType, agentId, range);

        List<List<T>> intermediate = hbaseOperations2.findParallel(NaverhBaseTables.AGENT_STAT_VER2_AGGRE, scan, this.operationFactory.getRowKeyDistributor(), mapper, APPLICATION_STAT_NUM_PARTITIONS);
        int expectedSize = (int) (range.getRange() / HBaseTables.AGENT_STAT_TIMESPAN_MS);
        List<T> merged = new ArrayList<>(expectedSize);
        for (List<T> each : intermediate) {
            merged.addAll(each);
        }
        return merged;
    }

    <T extends AgentStatDataPoint> boolean agentStatExists(StatType statType, AgentStatMapperV2<T> mapper, String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("checking for stat data existence : agentId={}, {}", agentId, range);
        }

        int resultLimit = 20;
        Scan scan = this.createScan(statType, agentId, range, resultLimit);

        List<List<T>> result = hbaseOperations2.findParallel(NaverhBaseTables.AGENT_STAT_VER2_AGGRE, scan, this.operationFactory.getRowKeyDistributor(), resultLimit, mapper, APPLICATION_STAT_NUM_PARTITIONS);
        if (result.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    <T extends JoinStatBo, S extends SampledAgentStatDataPoint> List<S> getSampledStatList(StatType statType, SampledApplicationStatResultExtractor<T, S> resultExtractor, String applicationId, Range range) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (resultExtractor == null) {
            throw new NullPointerException("resultExtractor must not be null");
        }
        Scan scan = this.createScan(statType, applicationId, range);
        return hbaseOperations2.findParallel(NaverhBaseTables.AGENT_STAT_VER2_AGGRE, scan, this.operationFactory.getRowKeyDistributor(), resultExtractor, APPLICATION_STAT_NUM_PARTITIONS);
    }

    <T extends JoinStatBo> ApplicationStatMapper<T> createRowMapper(ApplicationStatDecoder<T> decoder, Range range) {
        TimestampFilter filter = new RangeTimestampFilter(range);
        return new ApplicationStatMapper<>(this.operationFactory, decoder, filter);
    }

    private Scan createScan(StatType statType, String agentId, Range range) {
        long scanRange = range.getTo() - range.getFrom();
        long expectedNumRows = ((scanRange - 1) / HBaseTables.AGENT_STAT_TIMESPAN_MS) + 1;
        if (range.getFrom() != AgentStatUtils.getBaseTimestamp(range.getFrom())) {
            expectedNumRows++;
        }
        if (expectedNumRows > MAX_SCAN_CACHE_SIZE) {
            return this.createScan(statType, agentId, range, MAX_SCAN_CACHE_SIZE);
        } else {
            // expectedNumRows guaranteed to be within integer range at this point
            return this.createScan(statType, agentId, range, (int) expectedNumRows);
        }
    }

    private Scan createScan(StatType statType, String agentId, Range range, int scanCacheSize) {
        Scan scan = this.operationFactory.createScan(agentId, statType, range.getFrom(), range.getTo());
        scan.setCaching(scanCacheSize);
        scan.setId("AgentStat_" + statType);
        scan.addFamily(HBaseTables.AGENT_STAT_CF_STATISTICS);
        return scan;
    }
}
