/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.manager.dao.mysql;

import com.navercorp.pinpoint.manager.dao.SpanCountStatDao;
import com.navercorp.pinpoint.manager.vo.Range;
import com.navercorp.pinpoint.manager.vo.SpanCount;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlSpanCountStatDao implements SpanCountStatDao {

    private static final String NAMESPACE = SpanCountStatDao.class.getPackage().getName() + "." + SpanCountStatDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    public MysqlSpanCountStatDao(@Qualifier("statisticsSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate must not be null");
    }

    @Override
    public List<SpanCount> selectOrganizationSpanCount(String organzationName, Range range) {
        SearchCondition searchCondition = new SearchCondition(organzationName, range.getFromDateTime(), range.getToDateTime());
        return sqlSessionTemplate.selectList(NAMESPACE + "selectOrganizationSpanCount", searchCondition);
    }

    @Override
    public List<SpanCount> selectApplicationSpanCount(String organzationName, String applicationId, Range range) {
        SearchCondition searchCondition = new SearchCondition(organzationName, applicationId, range.getFromDateTime(), range.getToDateTime());
        return sqlSessionTemplate.selectList(NAMESPACE + "selectApplicationSpanCount", searchCondition);
    }

    @Override
    public List<SpanCount> selectAgentSpanCount(String organzationName, String applicationId, String agentId, Range range) {
        SearchCondition searchCondition = new SearchCondition(organzationName, applicationId, agentId, range.getFromDateTime(), range.getToDateTime());
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAgentSpanCount", searchCondition);
    }

    private class SearchCondition {
        private final static String EMPTY = "";
        private final String organizationName;
        private final String applicationId;
        private final String agentId;
        private final String from;
        private final String to;

        public SearchCondition(String organzationName, String from, String to) {
            this.organizationName = organzationName;
            this.from = from;
            this.to = to;
            this.applicationId = EMPTY;
            this.agentId = EMPTY;
        }

        public SearchCondition(String organzationName, String applicationId, String from, String to) {
            this.organizationName = organzationName;
            this.applicationId = applicationId;
            this.from = from;
            this.to = to;
            this.agentId = EMPTY;
        }

        public SearchCondition(String organzationName, String applicationId, String agentId, String from, String to) {
            this.organizationName = organzationName;
            this.applicationId = applicationId;
            this.agentId = agentId;
            this.from = from;
            this.to = to;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

}
