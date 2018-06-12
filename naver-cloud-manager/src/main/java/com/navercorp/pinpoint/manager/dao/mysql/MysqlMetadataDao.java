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
package com.navercorp.pinpoint.manager.dao.mysql;

import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.dao.mybatis.MetadataMapper;
import com.navercorp.pinpoint.manager.vo.PaaSOrganizationInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlMetadataDao implements MetadataDao {

    private static final String NAMESPACE = MetadataDao.class.getPackage().getName() + "." + MetadataDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("metaDataSqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;


    @Override
    public boolean existOrganization(String organizationName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "existOrganization", organizationName);
    }

    @Override
    public boolean createDatabase(String organizationName) {
        int result = sqlSessionTemplate.insert(MetadataMapper.class.getName()+".createDatabase", organizationName);

        if (result == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean insertOrganizationInfo(String organizationName) {
        int result = sqlSessionTemplate.insert(NAMESPACE+ "insertOrganizationInfo", new PaaSOrganizationInfo(organizationName, organizationName, organizationName, organizationName));

        if (result == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void dropDatabase(String organizationName) {
        sqlSessionTemplate.update(MetadataMapper.class.getName()+".dropDatabase", organizationName);
    }

    @Override
    public void deleteOrganizationInfo(String organizationName) {
        sqlSessionTemplate.delete(NAMESPACE+ "deleteOrganizationInfo", organizationName);
    }
}
