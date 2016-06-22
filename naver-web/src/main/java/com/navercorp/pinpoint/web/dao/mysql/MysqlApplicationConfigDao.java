/*
 * Copyright 2014 NAVER Corp.
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

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.AppAuthUserGroup;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlApplicationConfigDao implements ApplicationConfigDao {
    private static final String NAMESPACE = ApplicationConfigDao.class.getPackage().getName() + "." + ApplicationConfigDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;
    
    @Override
    public String insertAppAuthUserGroup(AppAuthUserGroup appAuth) {
        sqlSessionTemplate.insert(NAMESPACE + "insertAppAuthUserGroup", appAuth);
        return appAuth.getNumber();
    }

    @Override
    public void deleteAppAuthUserGroup(AppAuthUserGroup appAuth) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteAppAuthUserGroup", appAuth);
    }

    @Override
    public void updateAppAuthUserGroup(AppAuthUserGroup appAuth) {
        sqlSessionTemplate.update(NAMESPACE + "updateAppAuthUserGroup", appAuth);
    }

    @Override
    public List<AppAuthUserGroup> selectAppAuthUserGroupList(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAppAuthUserGroupList", applicationId);
    }

    @Override
    public String selectAppAuthConfiguration(String applicationId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectAppAuthConfig", applicationId);
    }
    
    @Override
    public boolean selectExistManager(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectExistManager", userId);
    }
    
}
