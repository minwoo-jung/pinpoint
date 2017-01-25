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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class TbaseFlatMapper implements FlatMapFunction<TBase, Tuple3<String, JoinStatBo, Long>> {
    private final static Logger logger = LoggerFactory.getLogger(TbaseFlatMapper.class);
    private static ApplicationCache applicationCache;
    private static AgentStatBatchMapper agentStatBatchMapper;

    public void setAgentStatBatchMapper(AgentStatBatchMapper agentStatBatchMapper) {
        TbaseFlatMapper.agentStatBatchMapper = agentStatBatchMapper;
    }

    public void setApplicationCache(ApplicationCache applicationCache) {
        TbaseFlatMapper.applicationCache = applicationCache;
    }

    @Override
    public void flatMap(TBase tBase, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        if (tBase instanceof TAgentStatBatch) {
            final TAgentStatBatch tAgentStatBatch = (TAgentStatBatch) tBase;
            final AgentStatBo agentStatBo = agentStatBatchMapper.map(tAgentStatBatch);
            final long agentStartTimestamp = tAgentStatBatch.getStartTimestamp();
            //TODO : (minwoo) 다음 stap 의 joinagentstatbo 조합에도 agentstarttime을 전달해주면 좋을듯함.
            final JoinAgentStatBo joinAgentStatBo = JoinAgentStatBo.createJoinAgentStatBo(agentStatBo, agentStartTimestamp);
            out.collect(new Tuple3<String, JoinStatBo, Long>(joinAgentStatBo.getAgentId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));

            final ApplicationCache.ApplicationKey applicationKey = new ApplicationCache.ApplicationKey(joinAgentStatBo.getAgentId(), joinAgentStatBo.getAgentStartTimestamp());
            final String applicationId = applicationCache.findApplicationId(applicationKey);
            if (applicationId.equals(ApplicationCache.NOT_FOOUND_APP_ID)) {
                logger.warn("can't found application id");
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


}
