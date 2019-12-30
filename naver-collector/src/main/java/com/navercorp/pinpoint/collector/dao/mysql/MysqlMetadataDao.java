/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.collector.dao.mysql;

import com.navercorp.pinpoint.collector.dao.MetadataDao;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlMetadataDao implements MetadataDao {

    private static final String NAMESPACE = MetadataDao.class.getPackage().getName() + "." + MetadataDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;


    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectOrganizationInfo", organizationName);
    }

    @Override
    public PaaSOrganizationKey selectPaaSOrganizationkey(String key) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectOrganizationKey", key);
    }

    @Override
    public List<PaaSOrganizationLifeCycle> selectPaaSOrganizationLifeCycle() {
        return sqlSessionTemplate.selectList("selectPaaSOrganizationLifeCycle");
    }
}
