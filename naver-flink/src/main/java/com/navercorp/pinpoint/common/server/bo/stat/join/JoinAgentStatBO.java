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

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBo {
    private byte version = 1;
    private String agentId;
    private long timeStamp;
    private JoinCpuLoadBo joinCpuLoadBo;

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setJoinCpuLoadBo(JoinCpuLoadBo joinCpuLoadBo) {
        this.joinCpuLoadBo = joinCpuLoadBo;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public JoinCpuLoadBo getJoinCpuLoadBo() {
        return joinCpuLoadBo;
    }

    public byte getVersion() {
        return version;
    }

    public static JoinCpuLoadBo joinCpuLoadBoLIst(List<CpuLoadBo> cpuLoadBos) {
        return JoinCpuLoadBo.joinCpuLoadBoLIst(cpuLoadBos);
    }

    public static JoinAgentStatBo joinAgentStatBo(List<JoinAgentStatBo> joinAgentStatBoList) {
        JoinAgentStatBo newJoinAgentStatBo = new JoinAgentStatBo();
        int boCount = joinAgentStatBoList.size();
        if (boCount == 0) {
            return newJoinAgentStatBo;
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (JoinAgentStatBo joinAgentStatBo : joinAgentStatBoList) {
            joinCpuLoadBoList.add(joinAgentStatBo.getJoinCpuLoadBo());
        }

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList);
        newJoinAgentStatBo.setJoinCpuLoadBo(joinCpuLoadBo);
        newJoinAgentStatBo.setAgentId(joinCpuLoadBo.getAgentId());
        newJoinAgentStatBo.setTimeStamp(joinCpuLoadBo.getTimestamp());

        return newJoinAgentStatBo;

    }
}
