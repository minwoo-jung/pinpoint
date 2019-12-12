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
package com.navercorp.pinpoint.flink.job.span.stat;

import com.navercorp.pinpoint.flink.job.span.stat.function.SpanStatKeySelector;
import com.navercorp.pinpoint.flink.job.span.stat.function.SpanStatVoWindow;
import com.navercorp.pinpoint.flink.job.span.stat.function.Timestamp;
import com.navercorp.pinpoint.flink.job.span.stat.receiver.TcpSourceFunction;
import com.navercorp.pinpoint.flink.job.span.stat.service.SpanStatService;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentKey;
import com.navercorp.pinpoint.flink.vo.RawData;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author minwoo.jung
 */
public class SpanStatJob implements Serializable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        ParameterTool parameters = ParameterTool.fromArgs(args);
        new SpanStatJob().start(parameters);
    }

    public void start(ParameterTool parameters) throws Exception {
        logger.info("SpanStat aggregation job");

        final Bootstrap bootstrap = Bootstrap.getInstance(parameters.toMap());

        final TcpSourceFunction tcpSourceFunction = bootstrap.getTcpSourceFunction();
        final StreamExecutionEnvironment env = bootstrap.createStreamExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(parameters);
        DataStreamSource<RawData> rawData = env.addSource(tcpSourceFunction);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        final SpanStatService spanStatService = bootstrap.getSpanStatService();
        final SingleOutputStreamOperator<Tuple3<SpanStatAgentKey, Long, Long>> statOperator = rawData.flatMap(bootstrap.getTBaseFlatMapper());
        DataStream<Tuple3<SpanStatAgentKey, Long, Long>> SpanStatAgentAggregationData = statOperator.assignTimestampsAndWatermarks(new Timestamp())
            .keyBy(new SpanStatKeySelector())
            .window(TumblingEventTimeWindows.of(Time.milliseconds(SpanStatVoWindow.WINDOW_SIZE)))
            .allowedLateness(Time.milliseconds(SpanStatVoWindow.ALLOWED_LATENESS))
            .apply(new SpanStatVoWindow());
        SpanStatAgentAggregationData.writeUsingOutputFormat(spanStatService);

        env.execute("Aggregation Span Stat Data");
    }
}
