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
import com.navercorp.pinpoint.web.vo.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void insertUserAccount(UserAccount userAccount) {
        UserAccount encodedUserAccount = new UserAccount(userAccount.getUserId(), passwordEncoder.encode(userAccount.getPassword()));
        userAccountDao.insertUserAccount(encodedUserAccount);
    }

    @Override
    public void deleteUserAccount(String userId) {
        userAccountDao.deleteUserAccount(userId);
    }

    @Override
    public void updateUserAccount(UserAccount userAccount) {
        UserAccount encodedUserAccount = new UserAccount(userAccount.getUserId(), passwordEncoder.encode(userAccount.getPassword()));
        userAccountDao.updateUserAccount(encodedUserAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount selectUserAccount(String userId) {
        return userAccountDao.selectUserAccount(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistUserId(String userId) {
        return userAccountDao.isExistUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCollectPassword(UserAccount targetUserAccount) {
        UserAccount userAccount = userAccountDao.selectUserAccount(targetUserAccount.getUserId());
        return passwordEncoder.matches(targetUserAccount.getPassword(), userAccount.getPassword());
    }
}
