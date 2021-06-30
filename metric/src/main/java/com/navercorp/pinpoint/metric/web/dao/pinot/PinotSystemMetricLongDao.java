/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.dao.pinot;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricLongDao implements SystemMetricDao<Long> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String NAMESPACE = PinotSystemMetricLongDao.class.getPackage().getName() + "." + PinotSystemMetricLongDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public PinotSystemMetricLongDao(SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @Override
    public List<SystemMetric> getSystemMetric(QueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSystemMetric", queryParameter);
    }

    @Override
    public List<SampledSystemMetric<Long>> getSampledSystemMetric(QueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSampledSystemMetric", queryParameter);
    }

    @Override
    public List<SystemMetricPoint<Long>> getSampledSystemMetricData(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag) {
        SystemMetricDataSearchKey systemMetricDataSearchKey = new SystemMetricDataSearchKey(metricDataSearchKey, metricTag);
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSampledSystemMetricData", systemMetricDataSearchKey);
    }
}
