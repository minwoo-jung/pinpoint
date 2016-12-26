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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBo implements JoinStatBo {
    private String agentId;
    private long timestamp;
    private List<JoinCpuLoadBo> joinCpuLoadBoList;

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        this.joinCpuLoadBoList = joinCpuLoadBoList;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setTimestamp(long timeStamp) {
        this.timestamp = timeStamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<JoinCpuLoadBo> getJoinCpuLoadBoList() {
        return joinCpuLoadBoList;
    }

    public static JoinCpuLoadBo joinCpuLoadBoLIst(List<CpuLoadBo> cpuLoadBos) {
        return JoinCpuLoadBo.joinCpuLoadBoListForCpuLoadBoList(cpuLoadBos);
    }

    public static JoinAgentStatBo joinAgentStatBo(List<JoinAgentStatBo> joinAgentStatBoList) {
        JoinAgentStatBo newJoinAgentStatBo = new JoinAgentStatBo();
        int boCount = joinAgentStatBoList.size();
        if (boCount == 0) {
            return newJoinAgentStatBo;
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (JoinAgentStatBo joinAgentStatBo : joinAgentStatBoList) {
            joinCpuLoadBoList.addAll(joinAgentStatBo.getJoinCpuLoadBoList());
        }

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, joinCpuLoadBoList.get(0).getTimestamp());
        List<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<>();
        newJoinCpuLoadBoList.add(joinCpuLoadBo);
        newJoinAgentStatBo.setJoinCpuLoadBoList(newJoinCpuLoadBoList);
        newJoinAgentStatBo.setAgentId(joinCpuLoadBo.getAgentId());
        newJoinAgentStatBo.setTimestamp(joinCpuLoadBo.getTimestamp());

        return newJoinAgentStatBo;

    }

    public static List<JoinCpuLoadBo> convertJoinCpuLoadBoList(List<CpuLoadBo> cpuLoadBos) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();

        for(CpuLoadBo cpuLoadBo : cpuLoadBos) {
            JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.convertJoinCpuLoadBo(cpuLoadBo);
            joinCpuLoadBoList.add(joinCpuLoadBo);
        }

        return joinCpuLoadBoList;
    }

    public static JoinAgentStatBo createJoinAgentStatBo(AgentStatBo agentStatBo) {
        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        joinAgentStatBo.setAgentId(agentStatBo.getAgentId());
        JoinCpuLoadBo joinCpuLoadBo = joinAgentStatBo.joinCpuLoadBoLIst(agentStatBo.getCpuLoadBos());
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>();
        joinCpuLoadBoList.add(joinCpuLoadBo);
        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
        joinAgentStatBo.setTimestamp(joinCpuLoadBo.getTimestamp());
        //TODO : (minwoo) stat 가져올때 nullpinointexcpetion 대비해야함.
//                JoinTransactionBo joinTransactionBo = joinAgentStatBo.joinTransactionBos(agentStatBo.getTransactionBos());
//                JoinActiveTraceBo joinActiveTraceBo = joinAgentStatBo.joinActiveTraceBos(agentStatBo.getActiveTraceBos());
        return joinAgentStatBo;
    }
}
