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
package com.navercorp.pinpoint.collector.mapper.thrift.span;

import com.navercorp.pinpoint.collector.interceptor.SpanStatData.SpanStatKey;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStatBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author minwoo.jung
 */
public class TFSpanStatMapper {

    public TFSpanStatBatch map(List<Map.Entry<SpanStatKey, Long>> data) {
        List<TFSpanStat> tFSpanStatList = new ArrayList<TFSpanStat>(data.size());

        for (Map.Entry<SpanStatKey, Long> entry : data) {
            SpanStatKey spanStatKey = entry.getKey();
            long count = entry.getValue();
            tFSpanStatList.add(new TFSpanStat(spanStatKey.getOrganization(), spanStatKey.getApplicationId(), spanStatKey.getAgentId(), spanStatKey.getSpanTime(), count));
        }

        return new TFSpanStatBatch(tFSpanStatList);
    }
}
