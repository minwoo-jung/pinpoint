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
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author minwoo.jung
 */
public class SpanStatVoWindow extends RichWindowFunction<Tuple3<SpanStatAgentKey, Long, Long>, Tuple3<SpanStatAgentKey, Long, Long>, SpanStatAgentKey, TimeWindow> {
    public static final int WINDOW_SIZE = 60000;
    public static final int ALLOWED_LATENESS = 180000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void apply(SpanStatAgentKey spanStatAgentKey, TimeWindow window, Iterable<Tuple3<SpanStatAgentKey, Long, Long>> values, Collector<Tuple3<SpanStatAgentKey, Long, Long>> out) throws Exception {
        final Tuple3<SpanStatAgentKey, Long, Long> joinTuple = join(values, spanStatAgentKey);

        long delayTime = new Date().getTime() - joinTuple.f2;
        if (logger.isDebugEnabled()) {
            if (delayTime > 180000) {
                logger.debug("[join][delay3] {} : {}, count({})", new Date(joinTuple.f2), joinTuple.f0, joinTuple.f1);
            } else if (delayTime > 120000) {
                logger.debug("[join][delay2] {} : {}, count({})", new Date(joinTuple.f2), joinTuple.f0, joinTuple.f1);
            } else if (delayTime > 60000) {
                logger.debug("[join][delay1] {} : {}, count({})", new Date(joinTuple.f2), joinTuple.f0, joinTuple.f1);
            } else {
                logger.debug("[join][non] {} : {}, count({})", new Date(joinTuple.f2), joinTuple.f0, joinTuple.f1);
            }
        }

        out.collect(joinTuple);
    }

    private Tuple3<SpanStatAgentKey, Long, Long> join(Iterable<Tuple3<SpanStatAgentKey, Long, Long>> values, SpanStatAgentKey spanStatAgentKey) {
        Long count = 0L;
        long minTimestamp = Long.MAX_VALUE;

        for (Tuple3<SpanStatAgentKey, Long, Long> value : values) {
            count = count + value.f1;

            if (value.f2 < minTimestamp) {
                minTimestamp = value.f2;
            }
        }

        return new Tuple3<SpanStatAgentKey, Long, Long>(spanStatAgentKey, count, minTimestamp);
    }
}
