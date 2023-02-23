/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class DefaultAgentStatService implements AgentStatService {

    private final AgentStatDao agentStatDao;

    public DefaultAgentStatService(AgentStatDao agentStatDao) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
    }

    @Override
    public SystemMetricData<? extends Number> selectAgentStat(String agentId, String chartType, TimeWindow timeWindow) {
        return agentStatDao.selectAgentStat(agentId, chartType, timeWindow);
    }

}
