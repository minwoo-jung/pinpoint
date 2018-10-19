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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.UserAccountDao;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserInformation;
import com.navercorp.pinpoint.web.vo.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserInformationServiceImpl implements UserInformationService {


    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private RoleService roleService;

    @Override
    @Transactional(readOnly = true)
    public boolean isExistUserId(String userId) {
        boolean isExistUserAccount = userAccountService.isExistUserId(userId);
        boolean isExistuserProfile = userService.isExistUserId(userId);

        return isExistUserAccount && isExistuserProfile;
    }

    @Override
    @Transactional(readOnly = true)
    public UserInformation selectUserInformation(String userId) {
        User user = userService.selectUserByUserId(userId);
        UserRole userRole = roleService.selectUserRole(userId);
        return new UserInformation(user, userRole);
    }

    @Override
    public void insertUserInformation(UserInformation userInformation) {
        userService.insertUser(userInformation.getProfile());
        userAccountService.insertUserAccount(userInformation.getAccount());
        roleService.insertUserRole(userInformation.getRole());
    }

    @Override
    public void deleteUserInformation(String userId) {
        userAccountService.deleteUserAccount(userId);
        userService.deleteUser(userId);
        roleService.deleteUserRole(userId);
    }
}
