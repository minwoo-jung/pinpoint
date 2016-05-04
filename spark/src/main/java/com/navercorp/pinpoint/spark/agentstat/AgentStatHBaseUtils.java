/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.spark.agentstat;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.server.bo.ActiveTraceHistogramBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

public class AgentStatHBaseUtils {
    public static List<AgentStat> toAgentStat(Result result) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        final byte[] rowKey = getKeyDistributor().getOriginalKey(result.getRow());
        final String agentId = BytesUtils.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        final long reverseTimestamp = BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
        final long timestamp = TimeUtils.recoveryTimeMillis(reverseTimestamp);

        NavigableMap<byte[], byte[]> qualifierMap = result.getFamilyMap(AGENT_STAT_CF_STATISTICS);

        AgentStat agentStat = new AgentStat(agentId, timestamp);
        if (qualifierMap.containsKey(AGENT_STAT_COL_INTERVAL)) {
            agentStat.setCollectInterval(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_INTERVAL)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_TYPE)) {
            agentStat.setGcType(Bytes.toString(qualifierMap.get(AGENT_STAT_COL_GC_TYPE)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_COUNT)) {
            agentStat.setGcOldCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_COUNT)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_TIME)) {
            agentStat.setGcOldTime(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_TIME)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_USED)) {
            agentStat.setHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_USED)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_MAX)) {
            agentStat.setHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_MAX)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_USED)) {
            agentStat.setNonHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_USED)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_MAX)) {
            agentStat.setNonHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_MAX)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_JVM_CPU)) {
            agentStat.setJvmCpuUsage(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_JVM_CPU)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_SYS_CPU)) {
            agentStat.setSystemCpuUsage(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_SYS_CPU)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)) {
            agentStat.setSampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)) {
            agentStat.setSampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)) {
            agentStat.setUnsampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)) {
            agentStat.setUnsampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM)) {
            ActiveTraceHistogramBo activeTraceHistogramBo = new ActiveTraceHistogramBo(qualifierMap.get(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM));
            agentStat.setHistogramSchema(activeTraceHistogramBo.getHistogramSchemaType());
            agentStat.setActiveTraceCounts(activeTraceHistogramBo.getActiveTraceCountMap());
        }

        return Arrays.asList(agentStat);
    }
    
    public static Put createPut(AgentStat agentStat) {
        byte[] key = getDistributedRowKey(agentStat);

        Put put = new Put(key);

        final long collectInterval = agentStat.getCollectInterval();
        put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_INTERVAL, Bytes.toBytes(collectInterval));

        if (agentStat.getGcType() != null) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_GC_TYPE, Bytes.toBytes(agentStat.getGcType()));
        }
        if (agentStat.getGcOldCount() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_GC_OLD_COUNT, Bytes.toBytes(agentStat.getGcOldCount()));
        }
        if (agentStat.getGcOldTime() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_GC_OLD_TIME, Bytes.toBytes(agentStat.getGcOldTime()));
        }
        if (agentStat.getHeapUsed() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_HEAP_USED, Bytes.toBytes(agentStat.getHeapUsed()));
        }
        if (agentStat.getHeapMax() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_HEAP_MAX, Bytes.toBytes(agentStat.getHeapMax()));
        }
        if (agentStat.getNonHeapUsed() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_NON_HEAP_USED, Bytes.toBytes(agentStat.getNonHeapUsed()));
        }
        if (agentStat.getNonHeapMax() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_NON_HEAP_MAX, Bytes.toBytes(agentStat.getNonHeapMax()));
        }
        if (agentStat.getJvmCpuUsage() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_JVM_CPU, Bytes.toBytes(agentStat.getJvmCpuUsage()));
        }
        if (agentStat.getSystemCpuUsage() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_SYS_CPU, Bytes.toBytes(agentStat.getSystemCpuUsage()));
        }
        if (agentStat.getSampledNewCount() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW, Bytes.toBytes(agentStat.getSampledNewCount()));
        }
        if (agentStat.getSampledContinuationCount() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION, Bytes.toBytes(agentStat.getSampledContinuationCount()));
        }
        if (agentStat.getUnsampledNewCount() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW, Bytes.toBytes(agentStat.getUnsampledNewCount()));
        }
        if (agentStat.getUnsampledContinuationCount() != AgentStat.NOT_COLLECTED) {
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION, Bytes.toBytes(agentStat.getUnsampledContinuationCount()));
        }
        
        Map<SlotType, Integer> countMap = agentStat.getActiveTraceCounts();
        if (countMap != null) {
            final Buffer buffer = new AutomaticBuffer();
            buffer.put((byte)0);
            buffer.putVar(agentStat.getHistogramSchema());
            buffer.putVar(countMap.size());
            buffer.putVar(countMap.get(SlotType.FAST));
            buffer.putVar(countMap.get(SlotType.NORMAL));
            buffer.putVar(countMap.get(SlotType.SLOW));
            buffer.putVar(countMap.get(SlotType.VERY_SLOW));
            
            put.addColumn(AGENT_STAT_CF_STATISTICS, AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM, buffer.getBuffer());
        }
        
        return put;
    }
    
    public static Scan[] getAgentStatScans(String agentId, long from, long to) throws IOException {
        Scan scan = new Scan();
        scan.setStartRow(getRowKey(agentId, to));
        scan.setStopRow(getRowKey(agentId, from));
        scan.addFamily(HBaseTables.AGENT_STAT_CF_STATISTICS);
        scan.setCaching(1000);

        return getKeyDistributor().getDistributedScans(scan);
    }
    
    
    private static RowKeyDistributorByHashPrefix getKeyDistributor() {
        return new RowKeyDistributorByHashPrefix(new RangeOneByteSimpleHash(0, 24, 32));
    }
    
    private static byte[] getRowKey(String agentId, long timestamp) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not null");
        }
        byte[] bAgentId = BytesUtils.toBytes(agentId);
        return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
    }

    private static byte[] getDistributedRowKey(AgentStat agentStat) {
        byte[] key = getRowKey(agentStat.getAgentId(), agentStat.getTimestamp());
        return getKeyDistributor().getDistributedKey(key);
    }
}
