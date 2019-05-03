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

import com.navercorp.pinpoint.flink.job.span.stat.mapper.SpanStatAgentKeyMapper;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentKey;
import com.navercorp.pinpoint.flink.vo.RawData;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStatBatch;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TBaseFlatMapper extends RichFlatMapFunction<RawData, Tuple3<SpanStatAgentKey, Long, Long>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public transient SpanStatAgentKeyMapper spanStatAgentKeyMapper = new SpanStatAgentKeyMapper();

    @Override
    public void open(Configuration parameters) throws Exception {
        spanStatAgentKeyMapper = new SpanStatAgentKeyMapper();
    }

    @Override
    public void flatMap(RawData rawData, Collector<Tuple3<SpanStatAgentKey, Long, Long>> out) throws Exception {
        final Object data = rawData.getData();
        if (!(data instanceof TFSpanStatBatch)) {
            logger.error("data is not TBase type {}", data);
            return;
        }

        TFSpanStatBatch tFSpanStatBatch = (TFSpanStatBatch)data;
        if (logger.isDebugEnabled()) {
            logger.debug("raw data : {}", tFSpanStatBatch);
        }

        List<Tuple3<SpanStatAgentKey, Long, Long>> outData = flatMap(tFSpanStatBatch);
        if (outData.size() == 0) {
            return;
        }

        for (Tuple3<SpanStatAgentKey, Long, Long> tuple : outData) {
            out.collect(tuple);
        }
    }

    private List<Tuple3<SpanStatAgentKey, Long, Long>> flatMap(TFSpanStatBatch tFSpanStatBatch) {
        List<TFSpanStat> tFSpanStatList = tFSpanStatBatch.getTFSpanStatList();
        List<Tuple3<SpanStatAgentKey, Long, Long>> spanStatAgentList = new ArrayList<>(tFSpanStatList.size());

        for (TFSpanStat tfSpanStat : tFSpanStatList) {
            try {
                spanStatAgentList.add(new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKeyMapper.map(tfSpanStat), tfSpanStat.getCount(), tfSpanStat.getSpanTime()));
            } catch (Exception e) {
                logger.error("occur exception while spanStatMapper processes data.", e);
            }
        }

        return spanStatAgentList;
    }
}
