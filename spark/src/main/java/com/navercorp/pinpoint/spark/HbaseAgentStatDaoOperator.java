/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.spark;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import org.apache.hadoop.hbase.client.Scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HbaseAgentStatDaoOperator {
    // backup
//    private <T extends AgentStatDataPoint> List<T> getAggregatedAgentStatList(AgentStatMapperV1<T> mapper, Aggregator<T> aggregator, String agentId, Range range) {
//        Scan scan = createScan(agentId, range);
//
//        List<List<T>> intermediate = hbaseOperations2.find(HBaseTables.AGENT_STAT_AGGR, scan, rowKeyDistributor, mapper);
//
//        List<T> merged = new ArrayList<>();
//
//        for (List<T> each : intermediate) {
//            merged.addAll(each);
//        }
//
//        Collections.sort(merged, Aggregator.TIMESTAMP_COMPARATOR);
//
//        List<Range> missingRanges = new ArrayList<>();
//        long last = range.getFrom();
//
//        for (T stat : merged) {
//            if (last + Aggregator.AGGR_SAMPLE_INTERVAL * 2 < stat.getTimestamp()) {
//                Range r = new Range(last, stat.getTimestamp() - Aggregator.AGGR_SAMPLE_INTERVAL);
//                missingRanges.add(r);
//            }
//
//            last = stat.getTimestamp();
//        }
//
//        if (last + Aggregator.AGGR_SAMPLE_INTERVAL * 2 < range.getTo()) {
//            Range r = new Range(last, range.getTo());
//            missingRanges.add(r);
//        }
//
//        for (Range r : missingRanges) {
//            logger.debug("AgentStatAggr doesn't have range: " + r.prettyToString() + " of " + agentId);
//
//            List<T> list = getAgentStatListFromRaw(mapper, agentId, r);
//
//            if (list.isEmpty()) {
//                logger.debug("AgentStat also doesn't have range: " + r.prettyToString() + " of " + agentId);
//                continue;
//            }
//
//            List<T> aggregated = aggregator.aggregate(list, Aggregator.AGGR_SAMPLE_INTERVAL);
//            merged.addAll(aggregated);
//        }
//
//        return merged;
//    }
}
