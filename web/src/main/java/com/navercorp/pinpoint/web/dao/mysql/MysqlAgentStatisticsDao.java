/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
@Transactional
public class MysqlAgentStatisticsDao implements AgentStatisticsDao {

    private static final String NAMESPACE = MysqlAgentStatisticsDao.class.getPackage().getName() + "." + MysqlAgentStatisticsDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public boolean insertAgentCount(AgentCountStatistics agentCountStatistics) {
        int insert = sqlSessionTemplate.insert(NAMESPACE + "insertAgentCount", agentCountStatistics);
        return insert > 0;
    }

    @Override
    public List<AgentCountStatistics> selectAgentCount(Range range) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAgentCount", range);
    }

}
