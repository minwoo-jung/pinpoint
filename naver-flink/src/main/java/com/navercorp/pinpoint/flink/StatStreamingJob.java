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

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.PooledHTableFactory;
import com.navercorp.pinpoint.flink.workcount.WordCountData;
import com.navercorp.pinpoint.flink.workcount.WorkCountStreamingJob;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * ==목표==
 *    stat 정보 조회시 시간을 길게하면 1주 이상 시간이 오래걸리는데
 *    적당히 데이터를 함축해서 1달 단위로 검색이 쉽게 되도록 하자.
 */
public class StatStreamingJob {
    public static void main(String[] args) throws Exception {

        // 평균을 구하고...
        // 분단위로 평균을 잘라보고
        // 결과 데이터를 hbase 에 데이터를 쌓는것을 만들어보고.
        // windows를 1분단위 로 묶고 그걸 다시 5분단위로 묶는다면 어떻게 될까... 즉 뭔가 가지를 두개로 뻗어나가게 한다면...
        // collector 데이터를 어떻게 가져올지 고민해야함.

        // min값 max 값을 어떻게 넣어두는게 좋을지 생각해야함.

        //tuple 안쓰고 model 객체를 넣어줌

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        final ParameterTool params = ParameterTool.fromArgs(args);
        env.getConfig().setGlobalJobParameters(params);


        DataStream<Tuple4<String, Integer, Integer, Integer>> counts =  env.fromCollection(getRawData())
                                                            .assignTimestampsAndWatermarks(new LinearTimestamp())
                                                            .keyBy(0)
                                                            .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                                                            .apply(new WindowFunction<Tuple2<String, Integer>, Tuple4<String, Integer, Integer, Integer>, Tuple, TimeWindow>() {
                                                                @Override
                                                                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple2<String, Integer>> values, Collector<Tuple4<String, Integer, Integer, Integer>> out) throws Exception {
                                                                    System.out.println("tuple data : " + tuple);
                                                                    Integer total = 0;
                                                                    Integer size = 0;
                                                                    String agentName = "null";
                                                                    for(Tuple2<String, Integer> value : values) {
                                                                        System.out.println("thread name "+ Thread.currentThread().getId() + ", value name : " + value.f0 + " int : " + value.f1);
                                                                        total = total + value.f1;
                                                                        size++;
                                                                        agentName = value.f0;
                                                                    }

                                                                    Integer average = total / size;
                                                                    System.out.println("############" + agentName +  " = total : " + total + " size : " + size + "  average : " + average);


                                                                    out.collect(new Tuple4<>(agentName, average, total, size));
                                                                }
                                                            });

        if (params.has("output")) {
            counts.writeAsText("F:\\workspace_intellij\\pinpointMinwoo\\output\\result11");
        } else {
            counts.writeUsingOutputFormat(new HBaseOutputFormat());
        }

        env.execute("Streaming WordCount");
    }

    public static class LinearTimestamp implements AssignerWithPunctuatedWatermarks<Tuple2<String, Integer>> {

        private static final long serialVersionUID = 1L;
        private long counter = 0L;

        @Override
        public long extractTimestamp(Tuple2<String, Integer> element, long previousElementTimestamp) {
            return counter += 49L;
        }

        @Override
        public Watermark checkAndGetNextWatermark(Tuple2<String, Integer> lastElement, long extractedTimestamp) {
            return new Watermark(counter - 1);
        }
    }

    /**
     *
     * This class implements an OutputFormat for HBase
     *
     */
    private static class HBaseOutputFormat implements OutputFormat<Tuple4<String, Integer, Integer, Integer>> {

        public static final TableName STAT_METADATA_FLINK = TableName.valueOf("StatMetaData_flink");
        public static final byte[] STAT_METADATA_CF = Bytes.toBytes("stat");
        public static final byte[] CPU_RATE = Bytes.toBytes("cpu_rate");
        private HbaseTemplate2 hbaseTemplate2 = null;
        private String taskNumber = null;
        private int rowNumber = 0;

        private static final long serialVersionUID = 1L;

        @Override
        public void configure(Configuration parameters) {
            org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", "alpha.zk.pinpoint.navercorp.com");
            conf.set("hbase.zookeeper.property.clientPort", "2181");

            hbaseTemplate2 = new HbaseTemplate2();
            hbaseTemplate2.setConfiguration(conf);
            hbaseTemplate2.setTableFactory(new PooledHTableFactory(conf));
            hbaseTemplate2.afterPropertiesSet();
        }

        @Override
        public void open(int taskNumber, int numTasks) throws IOException {
            System.out.println("taskNumber : " + taskNumber + " numTask :" + numTasks);
        }

        @Override
        public void writeRecord(Tuple4<String, Integer, Integer, Integer> statData) throws IOException {
            String rowKey = statData.f0 + System.currentTimeMillis();
            Put put = new Put(rowKey.getBytes());
            String value = "agentName : " + statData.f0 +  "average : " + statData.f1 + " total + " + statData.f2 + " + size + " + statData.f3;
            System.out.println(value);
            byte[] sqlBytes = Bytes.toBytes(value);
            put.addColumn(STAT_METADATA_CF, CPU_RATE, value.getBytes());
            hbaseTemplate2.put(STAT_METADATA_FLINK, put);
        }

        @Override
        public void close() throws IOException {
            if (hbaseTemplate2 != null) {
                try {
                    hbaseTemplate2.destroy();
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private static Collection<Tuple2<String, Integer>> getRawData() {
        final String AGENT_1 = "agent1";
        final String AGENT_2 = "agent2";
        final String AGENT_3 = "agent3";
        final String AGENT_4 = "agent4";
        final String AGENT_5 = "agent4";

        Collection<Tuple2<String, Integer>> rawData = new ArrayList<>();
        // window 1
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 1));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 2));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 3));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 4));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 5));

        rawData.add(new Tuple2<String, Integer>(AGENT_2, 101));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 102));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 103));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 104));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 105));

        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1001));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1002));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1003));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1004));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1005));

        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10001));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10002));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10003));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10004));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10005));

//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100001));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100002));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100003));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100004));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100005));

        //window2
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 11));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 12));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 13));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 14));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 15));

        rawData.add(new Tuple2<String, Integer>(AGENT_2, 111));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 112));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 113));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 114));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 115));

        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1011));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1012));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1013));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1014));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1015));

        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10011));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10012));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10013));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10014));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10015));

//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100011));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100012));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100013));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100014));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100015));

        //window3
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 21));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 22));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 23));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 24));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 25));

        rawData.add(new Tuple2<String, Integer>(AGENT_2, 121));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 122));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 123));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 124));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 125));

        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1021));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1022));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1023));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1024));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1025));

        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10021));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10022));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10023));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10024));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10025));

//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100021));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100022));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100023));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100024));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100025));

        //window4
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 31));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 32));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 33));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 34));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 35));

        rawData.add(new Tuple2<String, Integer>(AGENT_2, 131));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 132));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 133));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 134));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 135));

        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1031));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1032));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1033));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1034));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1035));

        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10031));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10032));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10033));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10034));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10035));

//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100031));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100032));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100033));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100034));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100035));

        //window 5
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 41));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 42));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 43));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 44));
        rawData.add(new Tuple2<String, Integer>(AGENT_1, 45));

        rawData.add(new Tuple2<String, Integer>(AGENT_2, 141));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 142));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 143));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 144));
        rawData.add(new Tuple2<String, Integer>(AGENT_2, 145));

        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1041));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1042));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1043));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1044));
        rawData.add(new Tuple2<String, Integer>(AGENT_3, 1045));

        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10041));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10042));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10043));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10044));
        rawData.add(new Tuple2<String, Integer>(AGENT_4, 10045));

//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100041));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100042));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100043));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100044));
//        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100045));

        return rawData;
    }
}
