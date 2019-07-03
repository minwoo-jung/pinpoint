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

package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.manager.dao.RepositoryDao;
import com.navercorp.pinpoint.manager.domain.mysql.repository.role.RoleInformation;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.User;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserAccount;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.UserRole;
import com.navercorp.pinpoint.manager.exception.user.DuplicateUserException;
import com.navercorp.pinpoint.manager.vo.user.AdminInfo;
import org.apache.hadoop.hbase.client.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
@Service
@Transactional(transactionManager="transactionManager", readOnly = true)
public class UserServiceImpl implements UserService {

    private final RepositoryDao repositoryDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(RepositoryDao repositoryDao,
                           PasswordEncoder passwordEncoder) {
        this.repositoryDao = Objects.requireNonNull(repositoryDao, "repositoryDao must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
    }

    @Override
    public List<AdminInfo> getAdmins() {
        List<User> admins = repositoryDao.selectAdmins();
        if (CollectionUtils.isEmpty(admins)) {
            return Collections.emptyList();
        }
        return admins.stream()
                .map(AdminInfo::create)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(transactionManager="transactionManager", readOnly = false)
    public void addAdmin(User user, String password) {
        final String userId = user.getUserId();
        if (repositoryDao.userExists(userId)) {
            throw new DuplicateUserException(userId);
        }
        repositoryDao.insertUser(user);

        final String encodedPassword = passwordEncoder.encode(password);
        final UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setPassword(encodedPassword);
        repositoryDao.insertUserAccount(userAccount);

        final UserRole userRole = new UserRole();
        final String adminRoleId = RoleInformation.ADMIN_ROLE.getRoleId();
        userRole.setUserId(userId);
        userRole.setRoleList(Arrays.asList(adminRoleId));
        repositoryDao.insertUserRole(userRole);

    }
}
