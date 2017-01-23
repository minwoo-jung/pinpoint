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
package com.navercorp.pinpoint.flink.process;

import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.web.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.hadoop.shaded.org.jboss.netty.util.internal.ConcurrentHashMap;
import org.apache.flink.util.Collector;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class TbaseFlatMapper implements FlatMapFunction<TBase, Tuple3<String, JoinStatBo, Long>> {
    private final static String NOT_FOOUND_APP_ID = "notFoundId";
    private static AgentStatBatchMapper agentStatBatchMapper;
    //TODO : (minwoo) 추후 cache 로직 구현 필요함.
    private static Map<Application, String> applicationInfoCache = new ConcurrentHashMap<>();
    private static AgentInfoMapper agentInfoMapper = new AgentInfoMapper();
    private static HbaseTemplate2 hbaseTemplate2;

    public TbaseFlatMapper(AgentStatBatchMapper agentStatBatchMapper, HbaseTemplate2 hbaseTemplate2) {
        //TODO : (minwoo) AgentStatBatchMapper 를 한번만 생성해서 문제는 없으나. 더 깔끔하게 개발할 필요는 있음, serialize 로 그냥 만들어버리면 동기화 필요없음.
        synchronized (TbaseFlatMapper.class) {
            if (this.agentStatBatchMapper == null) {
                this.agentStatBatchMapper = agentStatBatchMapper;
            }
            if (this.hbaseTemplate2 == null) {
                this.hbaseTemplate2 = hbaseTemplate2;
            }
        }
    }


    @Override
    public void flatMap(TBase tBase, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        if (tBase instanceof TAgentStatBatch) {
            TAgentStatBatch tAgentStatBatch = (TAgentStatBatch) tBase;
            final AgentStatBo agentStatBo = agentStatBatchMapper.map(tAgentStatBatch);
            final long agentStartTimestamp = tAgentStatBatch.getStartTimestamp();
            //TODO : (minwoo) 다음 stap 의 joinagentstatbo 조합에도 agentstarttime을 전달해주면 좋을듯함.
            JoinAgentStatBo joinAgentStatBo = JoinAgentStatBo.createJoinAgentStatBo(agentStatBo, agentStartTimestamp);
            out.collect(new Tuple3<String, JoinStatBo, Long>(joinAgentStatBo.getAgentId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));

            String applicationId = applicationInfoCache.get(new Application(joinAgentStatBo.getAgentId(), joinAgentStatBo.getAgentStartTimestamp()));
            if (applicationId == null) {
                applicationId = findApplicationId(joinAgentStatBo);
            }
            if (applicationId.equals(NOT_FOOUND_APP_ID)) {
                System.out.println("can't found application id");
                return;
            }
            JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
            List<JoinCpuLoadBo> joinCpuLoadBoList = JoinApplicationStatBo.createJoinCpuLoadBoList(agentStatBo);
            joinApplicationStatBo.setApplicationId(applicationId);
            joinApplicationStatBo.setTimestamp(joinAgentStatBo.getTimestamp());
            joinApplicationStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
            out.collect(new Tuple3<String, JoinStatBo, Long>(applicationId, joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
        }
    }

    private String findApplicationId(JoinAgentStatBo joinAgentStatBo) {
        final String agentId = joinAgentStatBo.getAgentId();
        final long agentStartTimestamp = joinAgentStatBo.getAgentStartTimestamp();
        final byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(Bytes.toBytes(agentId), HBaseTables.AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(agentStartTimestamp));

        Get get = new Get(rowKey);
        get.addColumn(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
        AgentInfo agentInfo = null;
        try {
            agentInfo = hbaseTemplate2.get(HBaseTables.AGENTINFO, get, agentInfoMapper);
        } catch (Exception e) {
            System.out.println("can't found application id" + e);
        }
        String applicationId = NOT_FOOUND_APP_ID;

        if (agentInfo != null) {
            applicationId = agentInfo.getApplicationName();
        }
        applicationInfoCache.put(new Application(agentId, agentStartTimestamp), applicationId);

        return applicationId;
    }

    private class Application {
        private String agentId;
        private long agentStartTime;

        public Application(String agentId, long agentStartTime) {
            this.agentId = agentId;
            this.agentStartTime = agentStartTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Application that = (Application) o;

            if (agentStartTime != that.agentStartTime) return false;
            return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

        }

        @Override
        public int hashCode() {
            int result = agentId != null ? agentId.hashCode() : 0;
            result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
            return result;
        }
    }
}
