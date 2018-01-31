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

import com.navercorp.pinpoint.web.dao.UserConfigDao;
import com.navercorp.pinpoint.web.vo.UserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserConfigServiceImpl implements com.navercorp.pinpoint.web.service.UserConfigService {

    @Autowired
    UserConfigDao userConfigDao;

    @Override
    public void insertUserConfiguration(UserConfiguration userConfiguration) {
        userConfigDao.insertUserConfiguration(userConfiguration);
    }

    @Override
    public void deleteUserConfiguration(String userId) {
        userConfigDao.deleteUserConfiguration(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserConfiguration selectUserConfiguration(String userId) {
        return userConfigDao.selectUserConfiguration(userId);
    }

    @Override
    public void updateUserConfiguration(UserConfiguration userConfiguration) {
        userConfigDao.updateUserConfiguration(userConfiguration);
    }
}
