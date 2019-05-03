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
package com.navercorp.pinpoint.flink.job.span.stat.dao.mysql;

import com.navercorp.pinpoint.flink.job.span.stat.dao.SpanStatAgentDao;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentVo;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SpanStatAgentDaoImpl implements SpanStatAgentDao {

    private static final String NAMESPACE = SpanStatAgentDao.class.getPackage().getName() + "." + SpanStatAgentDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public SpanStatAgentDaoImpl(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate must not be null");
    }

    @Override
    public void insertSpanStatAgentVo(SpanStatAgentVo spanStatAgentVo) {
        sqlSessionTemplate.insert(NAMESPACE + "insertSpanStatAgentVo", spanStatAgentVo);
    }
}
