/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.flink.job.span.stat.process;

import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentKey;
import com.navercorp.pinpoint.flink.vo.RawData;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStatBatch;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class TBaseFlatMapperTest {

    @Test
    public void flatMapTtest() throws Exception {
        String organization = "naver";
        String applicationName = "applicationName";
        long spanTime = 1566453840000L;
        TFSpanStat tfSpanStat1 = new TFSpanStat(organization, applicationName, "agent1", spanTime, 101);
        TFSpanStat tfSpanStat2 = new TFSpanStat(organization, applicationName, "agent2", spanTime, 102);
        TFSpanStat tfSpanStat3 = new TFSpanStat(organization, applicationName, "agent3", spanTime, 103);
        TFSpanStat tfSpanStat4 = new TFSpanStat(organization, applicationName, "agent4", spanTime, 104);
        TFSpanStat tfSpanStat5 = new TFSpanStat(organization, applicationName, "agent5", spanTime, 105);
        List<TFSpanStat> tfSpanStatList = new ArrayList<>();
        tfSpanStatList.add(tfSpanStat1);
        tfSpanStatList.add(tfSpanStat2);
        tfSpanStatList.add(tfSpanStat3);
        tfSpanStatList.add(tfSpanStat4);
        tfSpanStatList.add(tfSpanStat5);
        TFSpanStatBatch tfSpanStatBatch = new TFSpanStatBatch(tfSpanStatList);

        TBaseFlatMapper tBaseFlatMapper = new TBaseFlatMapper();

        RawData rawData = new RawData(tfSpanStatBatch, new HashMap<>());

        List<Tuple3<SpanStatAgentKey, Long, Long>> dataList = new ArrayList<Tuple3<SpanStatAgentKey, Long, Long>>();
        Collector<Tuple3<SpanStatAgentKey, Long, Long>> collector = new ListCollector<Tuple3<SpanStatAgentKey, Long, Long>>(dataList);
        tBaseFlatMapper.flatMap(rawData, collector);
        assertEquals(dataList.size(), 5);

        for (Tuple3<SpanStatAgentKey, Long, Long> data : dataList) {
            assertEquals(data.f0.getOrganization(), organization);
            assertEquals(data.f0.getApplicationId(), applicationName);
            assertTrue(data.f0.getAgentId().startsWith("agent"));
            assertEquals(data.f2, new Long(spanTime));
        }
    }

}