/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBoMapper implements ThriftBoMapper<JoinCpuLoadBo, TFAgentStat> {

    @Override
    public JoinCpuLoadBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetCpuLoad()) {
            return null;
        }

        JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();

        joinCpuLoadBo.setId(tFAgentStat.getAgentId());
        joinCpuLoadBo.setTimestamp(tFAgentStat.getTimestamp());

        TFCpuLoad tFCpuLoad = tFAgentStat.getCpuLoad();
        joinCpuLoadBo.setJvmCpuLoad(tFCpuLoad.getJvmCpuLoad());
        joinCpuLoadBo.setMinJvmCpuLoad(tFCpuLoad.getJvmCpuLoad());
        joinCpuLoadBo.setMaxJvmCpuLoad(tFCpuLoad.getJvmCpuLoad());
        joinCpuLoadBo.setSystemCpuLoad(tFCpuLoad.getSystemCpuLoad());
        joinCpuLoadBo.setMinSystemCpuLoad(tFCpuLoad.getSystemCpuLoad());
        joinCpuLoadBo.setMaxSystemCpuLoad(tFCpuLoad.getSystemCpuLoad());
        return joinCpuLoadBo;
    }
}
