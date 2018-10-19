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
package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.UserAccountDao;
import com.navercorp.pinpoint.web.vo.UserAccount;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlUserAccountDao implements UserAccountDao {

    private static final String NAMESPACE = UserAccountDao.class.getPackage().getName() + "." + UserAccountDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public boolean isExistUserId(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "isExistUserId", userId);
    }

    @Override
    public void insertUserAccount(UserAccount userAccount) {
        sqlSessionTemplate.selectOne(NAMESPACE + "insertUserAccount", userAccount);
    }

    @Override
    public void deleteUserAccount(String userId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUserAccount", userId);
    }

    @Override
    public void updateUserAccount(UserAccount userAccount) {
        sqlSessionTemplate.update(NAMESPACE + "updateUserAccount", userAccount);
    }

    @Override
    public UserAccount selectUserAccount(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectUserAccount", userId);
    }
}
