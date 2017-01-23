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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.ApplicationCupLoadDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.AgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.CpuLoadChartGroup;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationStatChartServiceImpl implements ApplicationStatChartService {

    private ApplicationCupLoadDao applicationCpuLoadDao;


    @Override
    public AgentStatChartGroup selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }
        List<SampledCpuLoad> sampledCpuLoads = this.applicationCpuLoadDao.getApplicationStatList(applicationId, timeWindow);
        return new CpuLoadChartGroup(timeWindow, sampledCpuLoads);
    }
}
