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
import com.navercorp.pinpoint.inspector.web.definition.AggregationFunction;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPreProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricProcessorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.FieldPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.FieldProcessorManager;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.metric.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.metric.web.util.metric.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.web.util.metric.UncollectedDataCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
* @author minwoo.jung
 */
@Service
public class DefaultAgentStatService implements AgentStatService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatDao agentStatDao;
    private final YMLInspectorManager ymlInspectorManager;
    private final MetricProcessorManager metricProcessorManager;
    private final FieldProcessorManager fieldProcessorManager;

    public DefaultAgentStatService(AgentStatDao agentStatDao, YMLInspectorManager ymlInspectorManager, MetricProcessorManager metricProcessorManager, FieldProcessorManager fieldProcessorManager) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
        this.ymlInspectorManager = Objects.requireNonNull(ymlInspectorManager, "ymlInspectorManager");
        this.metricProcessorManager = Objects.requireNonNull(metricProcessorManager, "metricProcessorManager");
        this.fieldProcessorManager = Objects.requireNonNull(fieldProcessorManager, "fieldProcessorManager");
    }

    @Override
    public InspectorMetricData<Double> selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow){
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults =  selectAll(inspectorDataSearchKey, metricDefinition);

        List<MetricValue<Double>> metricValueList = new ArrayList<>(metricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                Future<List<SystemMetricPoint<Double>>> future = result.getFuture();
                List<SystemMetricPoint<Double>> doubleList = future.get();

                MetricValue<Double> doubleMetricValue = createInspectorMetricValue(timeWindow, result.getField(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<MetricValue<Double>> processedMetricValueList = postprocessMetricData(metricDefinition, metricValueList);
        List<Long> timeStampList = createTimeStampList(timeWindow);
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
    }

    public InspectorMetricData<Double> selectAgentStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow){
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());
        MetricDefinition newMetricDefinition = preProcess(inspectorDataSearchKey, metricDefinition);

        List<QueryResult> queryResults =  selectAll(inspectorDataSearchKey, newMetricDefinition);

        List<MetricValue<Double>> metricValueList = new ArrayList<>(newMetricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                Future<List<SystemMetricPoint<Double>>> future = result.getFuture();
                List<SystemMetricPoint<Double>> doubleList = future.get();

                MetricValue<Double> doubleMetricValue = createInspectorMetricValue(timeWindow, result.getField(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<MetricValue<Double>> processedMetricValueList = postprocessMetricData(newMetricDefinition, metricValueList);
        List<Long> timeStampList = createTimeStampList(timeWindow);

        여기서 데이터 확인 필요함.
//        List<List<MetricValue<Double>>> groupedMetricValueList = groupingMetricValue(processedMetricValueList);
//        여기서 데이터 확인 필요함.
        return null;
//        //return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
//        리턴 데이터 필요함.
    }

    private MetricDefinition preProcess(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        MetricPreProcessor metricPreProcessor = metricProcessorManager.getPreProcessor(metricDefinition.getPreProcess());
        return metricPreProcessor.preProcess(inspectorDataSearchKey, metricDefinition);
    }

    //TODO : (minwoo) metric 레벨의 process를 담당하는 객체를 벌도록 빼자
    private List<MetricValue<Double>> postprocessMetricData(MetricDefinition metricDefinition, List<MetricValue<Double>> metricValueList) {
        MetricPostProcessor postProcessor = metricProcessorManager.getPostProcessor(metricDefinition.getPostProcess());
        return postProcessor.postProcess(metricValueList);

    }

    //TODO : (minwoo) systemmetric쪽과 중복있음
    private List<Long> createTimeStampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>((int) timeWindow.getWindowRangeCount());

        for (Long timestamp : timeWindow) {
            timestampList.add(timestamp);
        }

        return timestampList;
    }

    //TODO : (minwoo) field 레벨의 process를 담당하는 객체를 벌도록 빼자
    private MetricValue<Double> createInspectorMetricValue(TimeWindow timeWindow, Field field,
                                                                                   List<SystemMetricPoint<Double>> sampledSystemMetricDataList,
                                                                                   UncollectedDataCreator<Double> uncollectedDataCreator) {

        FieldPostProcessor postProcessor = fieldProcessorManager.getPostProcessor(field.getPostProcess());
        List<SystemMetricPoint<Double>> postProcessedDataList = postProcessor.postProcess(sampledSystemMetricDataList);

        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<SystemMetricPoint<Double>> filledSystemMetricDataList = builder.build(postProcessedDataList);

        List<Double> valueList = filledSystemMetricDataList.stream()
                .map(SystemMetricPoint::getYVal)
                .collect(Collectors.toList());

        return new MetricValue<>(field.getFieldName(), field.getTags(), valueList);
    }

    private List<QueryResult> selectAll(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            //TODO : (minwoo) dao호출을 하나로 변경
            Future<List<SystemMetricPoint<Double>>> doubleFuture = null;
            if (AggregationFunction.AVG.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatAvg(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatSum(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else {
                throw new IllegalArgumentException("Unknown aggregation function : " + field.getAggregationFunction());
            }
            invokeList.add(new QueryResult(doubleFuture, field));
        }

        return invokeList;
    }

    //TODO : (minwoo) 이것도 metric 쪽과 공통화 필요함.
    private static class QueryResult {
        private final Future<List<SystemMetricPoint<Double>>> future;
        private final Field field;

        public QueryResult(Future<List<SystemMetricPoint<Double>>> future, Field field) {
            this.future = Objects.requireNonNull(future, "future");
            this.field = Objects.requireNonNull(field, "field");
        }

        public Future<List<SystemMetricPoint<Double>>> getFuture() {
            return future;
        }

        public Field getField() {
            return field;
        }

    }

}
