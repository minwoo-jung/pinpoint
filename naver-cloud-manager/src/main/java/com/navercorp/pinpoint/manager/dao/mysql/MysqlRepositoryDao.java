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

import com.navercorp.pinpoint.manager.dao.RepositoryDao;
import com.navercorp.pinpoint.manager.domain.mysql.repository.role.RoleInformation;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.User;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserAccount;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserRole;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository
public class MysqlRepositoryDao implements RepositoryDao {

    private static final String NAMESPACE = RepositoryDao.class.getPackage().getName() + "." + RepositoryDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    public MysqlRepositoryDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public List<User> selectAdmins() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUsersByRole", RoleInformation.ADMIN_ROLE.getRoleId());
    }

    @Override
    public boolean userExists(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "existUser");
    }

    @Override
    public void insertUser(User user) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUser", user);
    }

    @Override
    public void insertUserAccount(UserAccount userAccount) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUserAccount", userAccount);
    }

    @Override
    public void insertUserRole(UserRole userRole) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUserRole", userRole);
    }

    @Override
    public void insertRoleDefinition(RoleInformation roleInformation) {
        sqlSessionTemplate.insert(NAMESPACE + "insertRoleDefinition", roleInformation);
    }
}
