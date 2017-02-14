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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_STAT_TIMESPAN_MS;
/**
 * @author Minwoo Jung
 */
public class ApplicationStatHbaseOperationFactory {

    private final ApplicationStatRowKeyEncoder rowKeyEncoder;

    private final ApplicationStatRowKeyDecoder rowKeyDecoder;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    public ApplicationStatHbaseOperationFactory(
                ApplicationStatRowKeyEncoder rowKeyEncoder,
                ApplicationStatRowKeyDecoder rowKeyDecoder,
                @Qualifier("agentStatV2RowKeyDistributor") AbstractRowKeyDistributor rowKeyDistributor) {
        Assert.notNull(rowKeyEncoder, "rowKeyEncoder must not be null");
        Assert.notNull(rowKeyDecoder, "rowKeyDecoder must not be null");
        Assert.notNull(rowKeyDistributor, "rowKeyDistributor must not be null");
        this.rowKeyEncoder = rowKeyEncoder;
        this.rowKeyDecoder = rowKeyDecoder;
        this.rowKeyDistributor = rowKeyDistributor;
    }

    public  <T extends JoinStatBo> List<Put> createPuts(String applicationId, List<T> joinStatBoList, StatType statType, ApplicationStatSerializer applicationStatSerializer) {
        if (joinStatBoList == null || joinStatBoList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<T>> timeslots = slotApplicationStatDataPoints(joinStatBoList);
        List<Put> puts = new ArrayList<Put>();
        for (Map.Entry<Long, List<T>> timeslot : timeslots.entrySet()) {
            long baseTimestamp = timeslot.getKey();
            List<T> slottedApplicationStatDataPoints = timeslot.getValue();

            final ApplicationStatRowKeyComponent rowKeyComponent = new ApplicationStatRowKeyComponent(applicationId, statType, baseTimestamp);
            byte[] rowKey = this.rowKeyEncoder.encodeRowKey(rowKeyComponent);
            byte[] distributedRowKey = this.rowKeyDistributor.getDistributedKey(rowKey);

            Put put = new Put(distributedRowKey);
            applicationStatSerializer.serialize(slottedApplicationStatDataPoints, put, null);
            puts.add(put);
        }
        return puts;
    }

    public Scan createScan(String agentId, StatType statType, long startTimestamp, long endTimestamp) {
        // TODO : (minwoo) AGENT_STAT_TIMESPAN_MS 을 APPLICATION_STAT_TIMESPAN_MS 로 변경하고 nvaerhbasetables 객체로 하나 빼야함.
        final ApplicationStatRowKeyComponent startRowKeyComponent = new ApplicationStatRowKeyComponent(agentId, statType, AgentStatUtils.getBaseTimestamp(endTimestamp));
        final ApplicationStatRowKeyComponent endRowKeyComponenet = new ApplicationStatRowKeyComponent(agentId, statType, AgentStatUtils.getBaseTimestamp(startTimestamp) - AGENT_STAT_TIMESPAN_MS);
        byte[] startRowKey = this.rowKeyEncoder.encodeRowKey(startRowKeyComponent);
        byte[] endRowKey = this.rowKeyEncoder.encodeRowKey(endRowKeyComponenet);
        return new Scan(startRowKey, endRowKey);
    }

    public AbstractRowKeyDistributor getRowKeyDistributor() {
        return this.rowKeyDistributor;
    }

    public String getAgentId(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getAgentId();
    }

    public AgentStatType getAgentStatType(byte[] distributedRowKey) {
//        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
//        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getAgentStatType();
        return null;
    }

    public long getBaseTimestamp(byte[] distributedRowKey) {
        byte[] originalRowKey = this.rowKeyDistributor.getOriginalKey(distributedRowKey);
        return this.rowKeyDecoder.decodeRowKey(originalRowKey).getBaseTimestamp();
    }

    private <T extends JoinStatBo> Map<Long, List<T>> slotApplicationStatDataPoints(List<T> joinStatBoList) {
        Map<Long, List<T>> timeslots = new TreeMap<Long, List<T>>();
        for (T joinStatBo : joinStatBoList) {
            long timestamp = joinStatBo.getTimestamp();
            long timeslot = AgentStatUtils.getBaseTimestamp(timestamp);
            List<T> slottedDataPoints = timeslots.get(timeslot);
            if (slottedDataPoints == null) {
                slottedDataPoints = new ArrayList<T>();
                timeslots.put(timeslot, slottedDataPoints);
            }
            slottedDataPoints.add(joinStatBo);
        }
        return timeslots;
    }
}
