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

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.PooledHTableFactory;
import com.navercorp.pinpoint.common.server.bo.stat.*;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.flink.process.TBaseMapper;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
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
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TBase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ==목표==
 *    stat 정보 조회시 시간을 길게하면 1주 이상 시간이 오래걸리는데
 *    적당히 데이터를 함축해서 1달 단위로 검색이 쉽게 되도록 하자.
 */
public class StatStreamingVer2Job implements Serializable {


    public static void main(String[] args) throws Exception {
        new StatStreamingVer2Job().start();
    }

    public void start() throws Exception {
        // windows를 1분단위 로 묶고 그걸 다시 5분단위로 묶는다면 어떻게 될까... 즉 뭔가 가지를 두개로 뻗어나가게 한다면...
        //tuple 안쓰고 model 객체를 넣어줌
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        final ParameterTool params = ParameterTool.fromArgs(new String[0]);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setGlobalJobParameters(params);
        //DataStream<String> rawData = env.socketTextStream("10.113.132.150", 9600, "\n");

        DataStream<TBase> rawData = env.addSource(new TcpSourceFunction());


        final String[] SPRING_CONFIG_XML = new String[]{"applicationContext-collector.xml"};
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(SPRING_CONFIG_XML);
        AgentStatBatchMapper agentStatBatchMapper = appCtx.getBean("agentStatBatchMapper", AgentStatBatchMapper.class);
        TBaseMapper mapper = new TBaseMapper(agentStatBatchMapper);
        System.out.println("init tbasemapper addreess : " + mapper);
        DataStream<Tuple2<String, JoinAgentStatBo>> counts = rawData.map(mapper)
            .assignTimestampsAndWatermarks(new LinearTimestamp())
            .keyBy(0)
            .window(TumblingEventTimeWindows.of(Time.seconds(120)))
            .apply(new WindowFunction<Tuple3<String, JoinAgentStatBo, Long>, Tuple2<String, JoinAgentStatBo>, Tuple, TimeWindow>() {

                @Override
                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinAgentStatBo, Long>> values, Collector<Tuple2<String, JoinAgentStatBo>> out) throws Exception {
                    JoinAgentStatBo newJoinAgentStatBo = join(values);
                    out.collect(new Tuple2<>(newJoinAgentStatBo.getAgentId(), newJoinAgentStatBo));
                }

                private JoinAgentStatBo join(Iterable<Tuple3<String, JoinAgentStatBo, Long>> values) {
                    List<JoinAgentStatBo> joinAgentStatBoList =  new ArrayList<JoinAgentStatBo>();
                    for (Tuple3<String, JoinAgentStatBo, Long> value : values) {
                        joinAgentStatBoList.add(value.f1);
                    }

                    return JoinAgentStatBo.joinAgentStatBo(joinAgentStatBoList);
                }
            });

        counts.writeUsingOutputFormat(new HBaseOutputFormat());
        env.execute("Aggregation Stat Data");
    }

    public class LinearTimestamp implements AssignerWithPunctuatedWatermarks<Tuple3<String, JoinAgentStatBo, Long>> {

        private static final long serialVersionUID = 1L;

        @Override
        public Watermark checkAndGetNextWatermark(Tuple3<String, JoinAgentStatBo, Long> lastElement, long extractedTimestamp) {
            System.out.println("ThreadId(timestamp) : " + Thread.currentThread().getId());
            System.out.println("timestamp : " + new Date(lastElement.f2).toString() + "long value : " + lastElement.f2);
            System.out.println("address" + this.toString());
            return new Watermark(lastElement.f2);
        }

        @Override
        public long extractTimestamp(Tuple3<String, JoinAgentStatBo, Long> value, long previousElementTimestamp) {
            return value.f2;
        }
    }

    //TODO : (minwoo) 별도 클래스로 분리 필요함.
    private class HBaseOutputFormat implements OutputFormat<Tuple2<String, JoinAgentStatBo>> {

        public final byte[] STAT_METADATA_CF = Bytes.toBytes("S");
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
        public void writeRecord(Tuple2<String, JoinAgentStatBo> statData) throws IOException {
            final JoinAgentStatBo joinAgentStatBo = statData.f1;
            String rowKey = joinAgentStatBo.getAgentId() + "_" + AgentStatType.CPU_LOAD.getRawTypeCode() +"_" + joinAgentStatBo.getTimeStamp();
            System.out.println("key : " + rowKey);
            Put put = new Put(rowKey.getBytes());

            final Buffer valueBuffer = new AutomaticBuffer();
            valueBuffer.putByte(joinAgentStatBo.getVersion());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getJvmCpuLoad());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getMaxJvmCpuLoad());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getMinJvmCpuLoad());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getSystemCpuLoad());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getMaxSystemCpuLoad());
            valueBuffer.putDouble(joinAgentStatBo.getJoinCpuLoadBo().getMinSystemCpuLoad());

            final Buffer qualifierBuffer = new AutomaticBuffer(64);
            qualifierBuffer.putVLong(joinAgentStatBo.getTimeStamp());

            put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
            try {
                //TODO : (minwoo) async 사용해야 하지 않을까 이게 병목이 되지 않을까 고민해보는게 좋을듯.
                hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
            } catch (Exception e) {
                System.out.println("exception ~!" + e);
            }
            System.out.println("put execute!!!");

            joinAgentStatBo.setAgentId("asdf");
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
}
