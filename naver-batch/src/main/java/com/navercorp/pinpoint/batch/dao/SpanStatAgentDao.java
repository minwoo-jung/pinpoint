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
package com.navercorp.pinpoint.batch.dao;

import com.navercorp.pinpoint.batch.vo.ApplicationInfo;
import com.navercorp.pinpoint.common.util.DateUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Repository
public class SpanStatAgentDao {

    private static final String NAMESPACE = SpanStatAgentDao.class.getPackage().getName() + "." + SpanStatAgentDao.class.getSimpleName() + ".";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    public void deleteSpanStatAgent(ApplicationInfo applicationInfo, long boundaryTime) {
        SearchCondition searchCondition = new SearchCondition(applicationInfo.getOrganization(), applicationInfo.getApplicationId(),DateUtils.longToDateStr(boundaryTime, DATE_TIME_FORMAT));
        sqlSessionTemplate.delete(NAMESPACE + "deleteSpanStatAgent", searchCondition);
    }

    public List<ApplicationInfo> selectApplicationList() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectApplicationList");
    }

    private class SearchCondition {
        private final String organization;
        private final String applicationId;
        private final String boundaryTime;

        public SearchCondition(String organzation, String applicationId, String boundaryTime) {
            this.organization = organzation;
            this.applicationId = applicationId;
            this.boundaryTime = boundaryTime;
        }

        public String getOrganization() {
            return organization;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getBoundaryTime() {
            return boundaryTime;
        }
    }
}
