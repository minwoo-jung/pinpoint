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
package com.navercorp.pinpoint.flink;

/**
 * @author minwoo.jung
 */

import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.PooledHTableFactory;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.function.Timestamp;
import com.navercorp.pinpoint.flink.process.ApplicationCache;
import com.navercorp.pinpoint.flink.process.TbaseFlatMapper;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.shaded.io.netty.bootstrap.*;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatStreamingVer2Job implements Serializable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        new StatStreamingVer2Job().start();
    }

    public void start() throws Exception {
        logger.info("start job");
        Bootstrap bootstrap = Bootstrap.getInstance();

        //TODO : (minwoo) 이것도 bootstrap을 빼면 될듯.
        final StreamExecutionEnvironment env = createStreamExecutionEnvironment();
        final ParameterTool params = ParameterTool.fromArgs(new String[0]);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setGlobalJobParameters(params);

        //TODO : (minwoo) statisticsDao도 spring bean으로 빼자.
        final StatisticsDao statisticsDao = bootstrap.getStatisticsDao();
        final TcpSourceFunction tcpSourceFunction = bootstrap.getTcpFuncation();
        final TbaseFlatMapper flatMapper = bootstrap.getTbaseFlatMapper();

        // set data source
//        final DataStream<TBase> rawData = env.addSource(tcpSourceFunction);
        DataStreamSource<TBase> rawData = env.addSource(tcpSourceFunction);
        setSourceFunctionParallel(rawData);

        // 0. generation rawdata
        final SingleOutputStreamOperator<Tuple3<String, JoinStatBo, Long>> statOperator = rawData.flatMap(flatMapper);

        // 1 process application stat raw data
        DataStream<Tuple3<String, JoinStatBo, Long>> applicationStatAggregationData = statOperator.filter(new FilterFunction<Tuple3<String, JoinStatBo, Long>>() {
            @Override
            public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
                if (value.f1 instanceof JoinApplicationStatBo) {
                    logger.info("1-1 FilterFunction : " + value.f1.toString());
                    return true;
                }

                return false;
            }
        })
            .assignTimestampsAndWatermarks(new Timestamp())
            .keyBy(0)
            .window(TumblingEventTimeWindows.of(Time.seconds(60)))
            .apply(new WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow>() {
                @Override
                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
                    try {
                        JoinApplicationStatBo joinApplicationStatBo = join(values);
                        logger.info("1-1 application stat aggre window function : " + joinApplicationStatBo);
                        out.collect(new Tuple3<>(joinApplicationStatBo.getId(), joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
                    } catch (Exception e) {
                        logger.error("window function error", e);
                    }
                }

                private JoinApplicationStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
                    List<JoinApplicationStatBo> joinApplicaitonStatBoList = new ArrayList<JoinApplicationStatBo>();
                    for (Tuple3<String, JoinStatBo, Long> value : values) {
                        joinApplicaitonStatBoList.add((JoinApplicationStatBo) value.f1);
                    }

                    return JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicaitonStatBoList);

                }
            });
        // 1-1 // 1 save data processing application stat raw data
        applicationStatAggregationData.writeUsingOutputFormat(statisticsDao);

        // 1-2. aggregate application stat data
        statOperator.filter(new FilterFunction<Tuple3<String, JoinStatBo, Long>>() {
            @Override
            public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
                if (value.f1 instanceof JoinApplicationStatBo) {
                    logger.info("1-2 application stat aggre window function : " + value.f1);
                    return true;
                }

                return false;
            }
            })
            .assignTimestampsAndWatermarks(new Timestamp())
            .keyBy(0)
            .window(TumblingEventTimeWindows.of(Time.seconds(120)))//TODO : (minwoo) 추후 5분으로 변경필요
            .apply(new WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow>() {
                @Override
                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
                    try {
                        JoinApplicationStatBo joinApplicationStatBo = join(values);
                        logger.info("1-2 application stat aggre window function : " + joinApplicationStatBo);
                        out.collect(new Tuple3<>(joinApplicationStatBo.getId(), joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO : (minwoo) 로깅 추가 필요함.
                    }
                }

                private JoinApplicationStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
                    List<JoinApplicationStatBo> joinApplicaitonStatBoList = new ArrayList<JoinApplicationStatBo>();
                    for (Tuple3<String, JoinStatBo, Long> value : values) {
                        joinApplicaitonStatBoList.add((JoinApplicationStatBo) value.f1);
                    }
                    return JoinApplicationStatBo.joinApplicationStatBo(joinApplicaitonStatBoList);

                }
            }).writeUsingOutputFormat(statisticsDao);


        // 2. agrregage agent stat
        statOperator.filter(new FilterFunction<Tuple3<String, JoinStatBo, Long>>() {
                @Override
                public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
                    if (value.f1 instanceof JoinAgentStatBo) {
                        logger.info("2 application stat aggre window function : " + value.f1);
                        return true;
                    }

                    return false;
                }
            })
            .assignTimestampsAndWatermarks(new Timestamp())
            .keyBy(0)
            .window(TumblingEventTimeWindows.of(Time.seconds(120)))//TODO : (minwoo) 추후 5분으로 변경필요

            .apply(new WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow>() {

                @Override
                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
                    try {
                        JoinAgentStatBo joinAgentStatBo = join(values);
                        logger.info("2 agent stat aggre window function : " + joinAgentStatBo);
                        out.collect(new Tuple3<>(joinAgentStatBo.getId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));
                    } catch (Exception e) {
                        e.printStackTrace();// TODO : (minwoo) 로깅 추가 필요함.
                    }
                }

                private JoinAgentStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
                    List<JoinAgentStatBo> joinAgentStatBoList =  new ArrayList<JoinAgentStatBo>();
                    for (Tuple3<String, JoinStatBo, Long> value : values) {
                        joinAgentStatBoList.add((JoinAgentStatBo) value.f1);
                    }

                    return JoinAgentStatBo.joinAgentStatBo(joinAgentStatBoList);
                }
            })
            .writeUsingOutputFormat(statisticsDao);

        env.execute("Aggregation Stat Data 2");
    }

    protected void setSourceFunctionParallel(DataStreamSource rawData) {
        FlinkConfiguration flinkConfiguration = Bootstrap.getInstance().getFlinkConfiguration();
        int parallel = flinkConfiguration.getFlinkSourceFunctionParallel();
        rawData.setParallelism(parallel);
    }

    protected StreamExecutionEnvironment createStreamExecutionEnvironment() {
        FlinkConfiguration flinkConfiguration = Bootstrap.getInstance().getFlinkConfiguration();

        if (flinkConfiguration.isLocalforFlinkStreamExecutionEnvironment()) {
            return StreamExecutionEnvironment.createLocalEnvironment();
        } else {
            return StreamExecutionEnvironment.getExecutionEnvironment();
        }
    }
}
