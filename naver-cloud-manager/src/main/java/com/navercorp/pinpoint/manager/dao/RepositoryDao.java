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

package com.navercorp.pinpoint.manager.dao;

import com.navercorp.pinpoint.manager.domain.mysql.repository.role.RoleInformation;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.User;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserAccount;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserRole;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface RepositoryDao {

    List<User> selectAdmins();

    boolean userExists(String userId);

    void insertUser(User user);

    void insertUserAccount(UserAccount userAccount);

    void insertUserRole(UserRole userRole);

    void insertRoleDefinition(RoleInformation roleInformation);
}
