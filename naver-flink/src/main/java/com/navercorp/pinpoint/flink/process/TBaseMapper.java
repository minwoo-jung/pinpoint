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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.thrift.TBase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author minwoo.jung
 */
public class TBaseMapper implements MapFunction<TBase, Tuple3<String, JoinAgentStatBo, Long>> {
    ////TODO : (minwoo) 동시성 처리 고민해야할듯함. 뭔가 init 처리하는게 있지 않을까 하는데... seralize 이슈하고 관련 있을듯.
    private AgentStatBatchMapper agentStatBatchMapper;

    public TBaseMapper(AgentStatBatchMapper agentStatBatchMapper) {
        this.agentStatBatchMapper = agentStatBatchMapper;
    }

    @Override
    public Tuple3<String, JoinAgentStatBo, Long> map(TBase tBase) throws Exception {

        if (tBase instanceof TAgentStatBatch) {
            JoinAgentStatBo joinAgentStatBo = joinTAgentStatBatch((TAgentStatBatch) tBase);
            return new Tuple3<String, JoinAgentStatBo, Long>(joinAgentStatBo.getAgentId(), joinAgentStatBo, joinAgentStatBo.getTimeStamp());
        }

        return new Tuple3<String, JoinAgentStatBo, Long>();
    }

    public JoinAgentStatBo joinTAgentStatBatch(TAgentStatBatch statBatch) {
        System.out.println("ThreadId(mapper) : " + Thread.currentThread().getId());
        System.out.println("this aapper address : " + this.toString());
        System.out.println("stat mapper address : " + this.agentStatBatchMapper);
        AgentStatBo agentStatBo = agentStatBatchMapper.map(statBatch);
        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        joinAgentStatBo.setAgentId(agentStatBo.getAgentId());
        JoinCpuLoadBo joinCpuLoadBo = joinAgentStatBo.joinCpuLoadBoLIst(agentStatBo.getCpuLoadBos());
        joinAgentStatBo.setJoinCpuLoadBo(joinCpuLoadBo);
        joinAgentStatBo.setTimeStamp(joinCpuLoadBo.getTimestamp());
        //TODO : (minwoo) stat 가져올때 nullpinointexcpetion 대비해야함.
//                JoinTransactionBo joinTransactionBo = joinAgentStatBo.joinTransactionBos(agentStatBo.getTransactionBos());
//                JoinActiveTraceBo joinActiveTraceBo = joinAgentStatBo.joinActiveTraceBos(agentStatBo.getActiveTraceBos());
        return joinAgentStatBo;
    }
}
