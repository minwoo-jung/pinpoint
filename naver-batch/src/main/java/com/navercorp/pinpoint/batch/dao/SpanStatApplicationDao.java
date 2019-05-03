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

import com.navercorp.pinpoint.batch.vo.TimeRange;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author minwoo.jung
 */
@Repository
public class SpanStatApplicationDao {

    private static final String NAMESPACE = SpanStatApplicationDao.class.getPackage().getName() + "." + SpanStatApplicationDao.class.getSimpleName() + ".";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public void insertSpanStatApplication(TimeRange timeRange) {
        sqlSessionTemplate.insert(NAMESPACE + "insertSpanStatApplication", timeRange);
    }

    public boolean existSpanStatApplication(TimeRange timeRange) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "existSpanStatApplication", timeRange);
    }
}
