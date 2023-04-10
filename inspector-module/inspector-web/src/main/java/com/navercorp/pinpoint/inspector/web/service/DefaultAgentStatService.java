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
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.web.mapping.Field;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValueGroup;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
* @author minwoo.jung
 */
public class DefaultAgentStatService implements AgentStatService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatDao agentStatDao;
    private final YMLInspectorManager ymlInspectorManager;

    public DefaultAgentStatService(AgentStatDao agentStatDao, YMLInspectorManager ymlInspectorManager) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
        this.ymlInspectorManager = Objects.requireNonNull(ymlInspectorManager, "ymlInspectorManager");
    }

    @Override
    public SystemMetricData<? extends Number> selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey){
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult<Number>> queryResults =  selectAll(inspectorDataSearchKey, metricDefinition);

        List<MetricValue<?>> metricValueList = new ArrayList<>(metricDefinition.getFields().size());

        try {
            for (QueryResult<Number> result : queryResults) {
                Future<List<SystemMetricPoint<Number>>> future = result.getFuture();
                List<SystemMetricPoint<Number>> systemMetricPoints = future.get();

                List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) (List<?>) systemMetricPoints;

                //데이터 변환 필요함.
                //MetricValue<Double> doubleMetricValue = createInspectorMetricValue(timeWindow, result.getTag(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);

//                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        //여기서 grouping 하기
        //List<MetricValueGroup<?>> metricValueGroupList = groupingMetricValue(metricValueList, groupingRule);
        return null;
    }

    private List<QueryResult<Number>> selectAll(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult<? extends Number>> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            Future<List<SystemMetricPoint<Double>>> doubleFuture = agentStatDao.selectAgentStat(inspectorDataSearchKey, metricDefinition.getName(), field);
            invokeList.add(new QueryResult<>(doubleFuture));
        }

        return (List<QueryResult<Number>>)(List<?>) invokeList;
    }

    //TODO : (minwoo) 이것도 metric 쪽과 공통화 필요함.
    private static class QueryResult<T extends Number> {
        private final Future<List<SystemMetricPoint<T>>> future;

        public QueryResult(Future<List<SystemMetricPoint<T>>> future) {
            this.future = Objects.requireNonNull(future, "future");
        }

        public Future<List<SystemMetricPoint<T>>> getFuture() {
            return future;
        }
    }

}
