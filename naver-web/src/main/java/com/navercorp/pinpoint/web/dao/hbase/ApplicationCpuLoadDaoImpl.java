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
package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.codec.stat.CpuLoadDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.web.dao.ApplicationCupLoadDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.v2.HbaseAgentStatDaoOperationsV2;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.CpuLoadSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationCpuLoadDaoImpl implements ApplicationCupLoadDao {

    @Autowired
    private CpuLoadDecoder cpuLoadDecoder;

    @Autowired
    private CpuLoadSampler cpuLoadSampler;

    @Autowired
    private HbaseApplicationStatDaoOperationsV2 operations;

    @Override
    public List<SampledCpuLoad> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<CpuLoadBo> mapper = operations.createRowMapper(cpuLoadDecoder, range);
        SampledAgentStatResultExtractor<CpuLoadBo, SampledCpuLoad> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, cpuLoadSampler);
        return operations.getSampledAgentStatList(AgentStatType.CPU_LOAD, resultExtractor, applicationId, range);
    }
}
