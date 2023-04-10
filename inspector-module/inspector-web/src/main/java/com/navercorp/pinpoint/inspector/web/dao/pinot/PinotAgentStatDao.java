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

package com.navercorp.pinpoint.inspector.web.dao.pinot;

import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameter;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.metric.web.mapping.Field;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * @author minwoo.jung
 */
public class PinotAgentStatDao implements AgentStatDao {

    private static final String NAMESPACE = PinotSystemMetricDoubleDao.class.getName() + ".";

    private final PinotAsyncTemplate asyncTemplate;

    public PinotAgentStatDao(PinotAsyncTemplate asyncTemplate) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
    }

    @Override
    public Future<List<SystemMetricPoint<Double>>> selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getName());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorAvgData", inspectorDataSearchKey);
    }
}
