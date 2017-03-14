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
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.CpuLoadSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.join.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class StatisticsDao implements OutputFormat<Tuple3<String, JoinStatBo, Long>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final byte[] STAT_METADATA_CF = Bytes.toBytes("S");

    private static final long serialVersionUID = 1L;
    private static HbaseTemplate2 hbaseTemplate2 = null;
    private static ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory;
    private static CpuLoadSerializer cpuLoadSerializer;

    private TableName AGENT_STAT_VER2_AGGRE;

    public StatisticsDao(HbaseTemplate2 hbaseTemplate2, ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory, CpuLoadSerializer cpuLoadSerializer) {
        this.hbaseTemplate2 = hbaseTemplate2;
        this.applicationStatHbaseOperationFactory = applicationStatHbaseOperationFactory;
        this.cpuLoadSerializer = cpuLoadSerializer;
    }

    @Override
    public void configure(Configuration parameters) {
        AGENT_STAT_VER2_AGGRE = TableName.valueOf("AgentStatV2Aggre");
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
    }

    @Override
    public void writeRecord(Tuple3<String, JoinStatBo, Long> statData) throws IOException {
        JoinStatBo joinStatBo = (JoinStatBo)statData.f1;
        if (joinStatBo instanceof JoinAgentStatBo) {
            logger.info("JoinAgentStatBo insert data : " + joinStatBo);
            insertJoinAgentStatBo((JoinAgentStatBo)joinStatBo);
        } else if (joinStatBo instanceof JoinApplicationStatBo) {
            logger.info("JoinApplicationStatBo insert data : " + joinStatBo);
            insertJoinApplicationStatBo((JoinApplicationStatBo)joinStatBo);
        }

    }

    private void insertJoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinApplicationStatBo.getJoinCpuLoadBoList();
        //TODO : (minwoo) 여러개의 raw가 아니라 30초 씩 묶어서 하나의 raw 에 저장하는것도 방법일듯.
        if (joinApplicationStatBo.getStatType() == StatType.APP_CPU_LOAD_AGGRE) {
//            logger.info("insert application aggre : " + new Date(joinApplicationStatBo.getTimestamp()) + " ("+ joinApplicationStatBo.getApplicationId() + " )");
        } else {
            logger.info("insert application raw data : " + new Date(joinApplicationStatBo.getTimestamp()) + " ("+ joinApplicationStatBo.getId() + " )");

            List<Put> cpuLoadPuts = applicationStatHbaseOperationFactory.createPuts(joinApplicationStatBo.getId(), joinApplicationStatBo.getJoinCpuLoadBoList(), StatType.APP_CPU_LOAD, cpuLoadSerializer);
            if (!cpuLoadPuts.isEmpty()) {
                List<Put> rejectedPuts = hbaseTemplate2.asyncPut(AGENT_STAT_VER2_AGGRE, cpuLoadPuts);
                if (CollectionUtils.isNotEmpty(rejectedPuts)) {
                    hbaseTemplate2.put(AGENT_STAT_VER2_AGGRE, rejectedPuts);
                }
            }
        }




        //TODO : (minwoo) 해야할 작업 리스트
        //1. cpu 리스트로 변경한다.
            //List<CpuLoadBo> cpuLoadBos;
        //2. cpuLoadSerializer을 application 용으로 하나 만들필요 있는지 검토 테이블 이름 때문에...
        //2. 리스트를 받아서 put 리스트로 변경한다.
        //3. put 리스트를 hbaseTemplate 에 전달한다.
            // this.hbaseTemplate.put(HBaseTables.테이블이름, rejectedPuts);

//        for (JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
//
//            String rowKey = joinApplicationStatBo.getApplicationId() + "_" + joinApplicationStatBo.getStatType().getRawTypeCode() +"_" + joinCpuLoadBo.getTimestamp();
//            Put put = new Put(rowKey.getBytes());
//
//            final Buffer valueBuffer = new AutomaticBuffer();
//            valueBuffer.putByte(joinCpuLoadBo.getVersion());
//            valueBuffer.putDouble(joinCpuLoadBo.getJvmCpuLoad());
//            valueBuffer.putDouble(joinCpuLoadBo.getMaxJvmCpuLoad());
//            valueBuffer.putDouble(joinCpuLoadBo.getMinJvmCpuLoad());
//            valueBuffer.putDouble(joinCpuLoadBo.getSystemCpuLoad());
//            valueBuffer.putDouble(joinCpuLoadBo.getMaxSystemCpuLoad());
//            valueBuffer.putDouble(joinCpuLoadBo.getMinSystemCpuLoad());
//
//            final Buffer qualifierBuffer = new AutomaticBuffer(64);
//            qualifierBuffer.putVLong(joinCpuLoadBo.getTimestamp());
//
//            put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
//            hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
//        }
    }

    private void insertJoinAgentStatBo(JoinAgentStatBo joinAgentStatBo) {
//        logger.info("insert agent data : " + new Date(joinAgentStatBo.getTimestamp()));
//        String rowKey = joinAgentStatBo.getAgentId() + "_" + AgentStatType.CPU_LOAD.getRawTypeCode() +"_" + joinAgentStatBo.getTimestamp();
//        Put put = new Put(rowKey.getBytes());
//
//        final Buffer valueBuffer = new AutomaticBuffer();
//        JoinCpuLoadBo joinCpuLoadBo = joinAgentStatBo.getJoinCpuLoadBoList().get(0);
//        valueBuffer.putByte(joinCpuLoadBo.getVersion());
//        valueBuffer.putDouble(joinCpuLoadBo.getJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMaxJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMinJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getSystemCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMaxSystemCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMinSystemCpuLoad());
//
//        final Buffer qualifierBuffer = new AutomaticBuffer(64);
//        qualifierBuffer.putVLong(joinAgentStatBo.getTimestamp());
//
//        put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
//        hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
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