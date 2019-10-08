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
import com.navercorp.pinpoint.batch.vo.TimeRange;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Repository
public class SpanStatApplicationDao {

    private static final String NAMESPACE = SpanStatApplicationDao.class.getPackage().getName() + "." + SpanStatApplicationDao.class.getSimpleName() + ".";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public void insertSpanStatApplication(ApplicationInfo applicationInfo, TimeRange timeRange) {
        ApplicationSearchCondition applicationSearchCondition = new ApplicationSearchCondition(applicationInfo.getOrganization(), applicationInfo.getApplicationId(), timeRange.getFromDateTime(), timeRange.getToDateTime());
        sqlSessionTemplate.insert(NAMESPACE + "insertSpanStatApplication", applicationSearchCondition);
    }

    public boolean existSpanStatApplication(ApplicationInfo applicationInfo, TimeRange timeRange) {
        ApplicationSearchCondition applicationSearchCondition = new ApplicationSearchCondition(applicationInfo.getOrganization(), applicationInfo.getApplicationId(), timeRange.getFromDateTime(), timeRange.getToDateTime());
        return sqlSessionTemplate.selectOne(NAMESPACE + "existSpanStatApplication", applicationSearchCondition);
    }

    public List<String> selectOrganizationList() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectOrganizationList");
    }


    private class ApplicationSearchCondition {
        private final String organization;
        private final String applicationId;
        private final String from;
        private final String to;


        public ApplicationSearchCondition(String organzation, String applicationId, String from, String to) {
            this.organization = organzation;
            this.applicationId = applicationId;
            this.from = from;
            this.to = to;
        }

        public String getOrganization() {
            return organization;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

}
