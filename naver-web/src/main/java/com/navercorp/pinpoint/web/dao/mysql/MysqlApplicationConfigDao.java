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

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public String insertAppUserGroupAuth(AppUserGroupAuth appAuth) {
        sqlSessionTemplate.insert(NAMESPACE + "insertAppUserGroupAuth", appAuth);
        return appAuth.getNumber();
    }

    @Override
    public void deleteAppUserGroupAuth(AppUserGroupAuth appAuth) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteAppUserGroupAuth", appAuth);
    }

    @Override
    public void updateAppUserGroupAuth(AppUserGroupAuth appAuth) {
        sqlSessionTemplate.update(NAMESPACE + "updateAppUserGroupAuth", appAuth);
    }

    @Override
    public List<AppUserGroupAuth> selectAppUserGroupAuthList(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAppUserGroupAuthList", applicationId);
    }

    @Override
    public List<AppUserGroupAuth> selectAppUserGroupAuthListByUserGroupId(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAppUserGroupAuthListByUserGroupId", userGroupId);
    }
}
