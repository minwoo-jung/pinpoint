/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TFAgentStatMappter {
    public final TFCpuLoadMapper tCpuLoadMapper = new TFCpuLoadMapper();

    public List<TFAgentStat> map(AgentStatBo agentStatBo) {

        List<CpuLoadBo> cpuLoadBoList = agentStatBo.getCpuLoadBos();
        List<TFAgentStat> tFAgentStatList = new ArrayList<>(agentStatBo.getCpuLoadBos().size());
        for(CpuLoadBo cpuLoadBo : cpuLoadBoList) {
            tFAgentStatList.add(createTagentStat(cpuLoadBo));
        }

        return tFAgentStatList;
    }

    private TFAgentStat createTagentStat(CpuLoadBo cpuLoadBo) {
        TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setCpuLoad(tCpuLoadMapper.map(cpuLoadBo));

        // TODO : (minwoo) 다른 데이터가(gc trasaction) 추가되면 더 방어적으로 agentid, starttime이 null이 아닌경우를 매번 체크해야함. 없다면 다른데이터에서 뽑아와야함.
        tFAgentStat.setAgentId(cpuLoadBo.getAgentId());
        tFAgentStat.setStartTimestamp(cpuLoadBo.getStartTimestamp());
        tFAgentStat.setTimestamp(cpuLoadBo.getTimestamp());
        // 추후에 collectInterval set 해주어야함. : transaction 에서 사용하므로..
        return tFAgentStat;
    }
}
