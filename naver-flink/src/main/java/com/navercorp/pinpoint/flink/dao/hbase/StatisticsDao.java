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
package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.PooledHTableFactory;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.join.*;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class StatisticsDao implements OutputFormat<Tuple3<String, JoinStatBo, Long>> {

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
    }

    @Override
    public void writeRecord(Tuple3<String, JoinStatBo, Long> statData) throws IOException {
        JoinStatBo joinStatBo = (JoinStatBo)statData.f1;
        if (joinStatBo instanceof JoinAgentStatBo) {
            insertJoinAgentStatBo((JoinAgentStatBo)joinStatBo);
        } else if (joinStatBo instanceof JoinApplicationStatBo) {
            insertJoinApplicationStatBo((JoinApplicationStatBo)joinStatBo);
        }

    }

    private void insertJoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        //TODO : (minwoo) 여러개의 raw가 아니라 30초 씩 묶어서 하나의 raw 에 저장하는것도 방법일듯.
        if (joinApplicationStatBo.getStatType() == StatType.APP_CPU_LOAD_AGGRE) {
            System.out.println("insert application aggre : " + new Date(joinApplicationStatBo.getTimestamp()));
        } else {
            System.out.println("insert application raw data : " + new Date(joinApplicationStatBo.getTimestamp()));
        }
        for (JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {

            String rowKey = joinApplicationStatBo.getApplicationId() + "_" + joinApplicationStatBo.getStatType().getRawTypeCode() +"_" + joinCpuLoadBo.getTimestamp();
            Put put = new Put(rowKey.getBytes());

            final Buffer valueBuffer = new AutomaticBuffer();
            valueBuffer.putByte(joinCpuLoadBo.getVersion());
            valueBuffer.putDouble(joinCpuLoadBo.getJvmCpuLoad());
            valueBuffer.putDouble(joinCpuLoadBo.getMaxJvmCpuLoad());
            valueBuffer.putDouble(joinCpuLoadBo.getMinJvmCpuLoad());
            valueBuffer.putDouble(joinCpuLoadBo.getSystemCpuLoad());
            valueBuffer.putDouble(joinCpuLoadBo.getMaxSystemCpuLoad());
            valueBuffer.putDouble(joinCpuLoadBo.getMinSystemCpuLoad());

            final Buffer qualifierBuffer = new AutomaticBuffer(64);
            qualifierBuffer.putVLong(joinCpuLoadBo.getTimestamp());

            put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
            hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
        }
    }

    private void insertJoinAgentStatBo(JoinAgentStatBo joinAgentStatBo) {
        System.out.println("insert agent data : " + new Date(joinAgentStatBo.getTimestamp()));
        String rowKey = joinAgentStatBo.getAgentId() + "_" + AgentStatType.CPU_LOAD.getRawTypeCode() +"_" + joinAgentStatBo.getTimestamp();
        Put put = new Put(rowKey.getBytes());

        final Buffer valueBuffer = new AutomaticBuffer();
        JoinCpuLoadBo joinCpuLoadBo = joinAgentStatBo.getJoinCpuLoadBoList().get(0);
        valueBuffer.putByte(joinCpuLoadBo.getVersion());
        valueBuffer.putDouble(joinCpuLoadBo.getJvmCpuLoad());
        valueBuffer.putDouble(joinCpuLoadBo.getMaxJvmCpuLoad());
        valueBuffer.putDouble(joinCpuLoadBo.getMinJvmCpuLoad());
        valueBuffer.putDouble(joinCpuLoadBo.getSystemCpuLoad());
        valueBuffer.putDouble(joinCpuLoadBo.getMaxSystemCpuLoad());
        valueBuffer.putDouble(joinCpuLoadBo.getMinSystemCpuLoad());

        final Buffer qualifierBuffer = new AutomaticBuffer(64);
        qualifierBuffer.putVLong(joinAgentStatBo.getTimestamp());

        put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
        hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
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