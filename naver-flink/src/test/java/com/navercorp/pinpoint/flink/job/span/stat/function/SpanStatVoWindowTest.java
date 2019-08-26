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

package com.navercorp.pinpoint.flink.job.span.stat.function;

import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentKey;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class SpanStatVoWindowTest {
    @Test
    public void applyTest() throws Exception {
        long spanTime = 1566453840000L;
        String organization = "organization";
        String applicationId = "applicationId";
        String agentId = "agentId";
        SpanStatAgentKey spanStatAgentKey = new SpanStatAgentKey(organization, applicationId, agentId);

        List<Tuple3<SpanStatAgentKey, Long, Long>> dataList = new ArrayList<Tuple3<SpanStatAgentKey, Long, Long>>();
        Tuple3<SpanStatAgentKey, Long, Long> data1 = new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, 101L, spanTime);
        Tuple3<SpanStatAgentKey, Long, Long> data2 = new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, 202L, spanTime + 2000L);
        Tuple3<SpanStatAgentKey, Long, Long> data3 = new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, 303L, spanTime + 3000L);
        Tuple3<SpanStatAgentKey, Long, Long> data4 = new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, 404L, spanTime + 4000L);
        Tuple3<SpanStatAgentKey, Long, Long> data5 = new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, 505L, spanTime + 5000L);
        dataList.add(data1);
        dataList.add(data2);
        dataList.add(data3);
        dataList.add(data4);
        dataList.add(data5);

        TimeWindow timeWindow = new TimeWindow(spanTime, spanTime + 300000L);

        List<Tuple3<SpanStatAgentKey, Long, Long>> resultDataList = new ArrayList<Tuple3<SpanStatAgentKey, Long, Long>>();
        Collector<Tuple3<SpanStatAgentKey, Long, Long>> collector = new ListCollector<Tuple3<SpanStatAgentKey, Long, Long>>(resultDataList);

        SpanStatVoWindow spanStatVoWindow = new SpanStatVoWindow();
        spanStatVoWindow.apply(spanStatAgentKey, timeWindow, dataList, collector);

        assertEquals(resultDataList.size(), 1);
        Tuple3<SpanStatAgentKey, Long, Long> result = resultDataList.get(0);
        assertTrue(result.f0.equals(spanStatAgentKey));
        assertEquals(result.f1, new Long(1515L));
        assertEquals(result.f2, new Long(spanTime));
    }
}