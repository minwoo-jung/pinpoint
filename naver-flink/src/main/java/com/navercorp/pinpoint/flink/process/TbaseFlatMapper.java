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
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.hadoop.shaded.org.jboss.netty.util.internal.ConcurrentHashMap;
import org.apache.flink.util.Collector;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class TbaseFlatMapper implements FlatMapFunction<TBase, Tuple3<String, JoinStatBo, Long>> {
    private static AgentStatBatchMapper agentStatBatchMapper;
    //TODO : (minwoo) 추후 cache 로직 구현 필요함.
    private static Map<String, String> applicationInfoCache = new ConcurrentHashMap<>();

    public TbaseFlatMapper(AgentStatBatchMapper agentStatBatchMapper) {
        //TODO : (minwoo) AgentStatBatchMapper 를 한번만 생성해서 문제는 없으나. 더 깔끔하게 개발할 필요는 있음, serialize 로 그냥 만들어버리면 동기화 필요없음.
        synchronized (TBaseMapper.class) {
            if (this.agentStatBatchMapper == null) {
                this.agentStatBatchMapper = agentStatBatchMapper;
            }
        }

        applicationInfoCache.put("agent_minwoo1", "app_minwoo");
        applicationInfoCache.put("agent_minwoo2", "app_minwoo");
        applicationInfoCache.put("minwoo_local", "app_minwoo");
    }


    @Override
    public void flatMap(TBase tBase, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        if (tBase instanceof TAgentStatBatch) {
            final AgentStatBo agentStatBo = agentStatBatchMapper.map((TAgentStatBatch) tBase);
            JoinAgentStatBo joinAgentStatBo = JoinAgentStatBo.createJoinAgentStatBo(agentStatBo);
            out.collect(new Tuple3<String, JoinStatBo, Long>(joinAgentStatBo.getAgentId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));

            final String applicationId = applicationInfoCache.get(joinAgentStatBo.getAgentId());
            JoinApplicationStatBo joinApplicationStatBo = new JoinApplicationStatBo();
            List<JoinCpuLoadBo> joinCpuLoadBoList = JoinApplicationStatBo.createJoinCpuLoadBoList(agentStatBo);
            joinApplicationStatBo.setApplicationId(applicationId);
            joinApplicationStatBo.setTimestamp(joinAgentStatBo.getTimestamp());
            joinApplicationStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
            out.collect(new Tuple3<String, JoinStatBo, Long>(applicationId, joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
        }
    }
}
