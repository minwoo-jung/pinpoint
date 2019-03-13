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
package com.navercorp.pinpoint.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.web.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Position;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class ApplicationConfigServiceImpl implements ApplicationConfigService {

    @Autowired
    private ApplicationConfigDao applicationConfigDao;
    
    @Autowired
    private UserGroupService userGroupService;

    @Override
    @Transactional(readOnly = true)
    public ApplicationConfiguration selectApplicationConfiguration(String applicationId) {
        List<AppUserGroupAuth> appAuthUserGroupList = applicationConfigDao.selectAppUserGroupAuthList(applicationId);
        return new ApplicationConfiguration(applicationId, appAuthUserGroupList);
    }
    
    @Override
    public void initApplicationConfiguration(String applicationId) {
        List<AppUserGroupAuth> appUserGroupAuthList = applicationConfigDao.selectAppUserGroupAuthList(applicationId);
        if (appUserGroupAuthList.isEmpty()) {
            AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, AppUserGroupAuth.Position.GUEST.toString(), Position.GUEST.toString(), new AppAuthConfiguration());
            applicationConfigDao.insertAppUserGroupAuth(appUserGroupAuth);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isManager(String userId) {
        return applicationConfigDao.isManager(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUserGroupAuth> selectApplicationConfigurationByUserGroupId(String userGroupId) {
        return applicationConfigDao.selectAppUserGroupAuthListByUserGroupId(userGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Position searchMyPosition(String applicationId, String userId) {
        ApplicationConfiguration appConfig = selectApplicationConfiguration(applicationId);
        Map<String, AppUserGroupAuth> appUserGroupAuthes = appConfig.getAppUserGroupAuthes();
        Map<String, UserGroup> myUserGroups = getUserGroups(userId); 
        AppUserGroupAuth.Position myPosition = AppUserGroupAuth.Position.GUEST;
        
        for(Map.Entry<String, AppUserGroupAuth> entry : appUserGroupAuthes.entrySet()) {
            String userGroupId = entry.getKey();
            if (myUserGroups.containsKey(userGroupId)) {
                Position position = entry.getValue().getPosition();
                if (position != null && position.isHigherOrEqualLevel(myPosition)){
                    myPosition = position;
                }
            }
        }
        
        return myPosition;
    }
    
    private Map<String, UserGroup> getUserGroups(String userId) {
        List<UserGroup> userGroupList = userGroupService.selectUserGroupByUserId(userId);
        Map<String, UserGroup> userGroups = new HashMap<String, UserGroup>(); 
        
        for (UserGroup userGroup : userGroupList) {
            userGroups.put(userGroup.getId(), userGroup);
        }
        
        return userGroups;
    }

    @Override
    public boolean isCanPreoccupancy(String applicationId) {
        ApplicationConfiguration appConfig = selectApplicationConfiguration(applicationId);

        if (appConfig.getAppUserGroupAuth().isEmpty()) {
            return true;
        }
        if (existManager(appConfig) == false) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditConfiguration(String applicationId, String userId) {
        AppUserGroupAuth.Position myPosition = searchMyPosition(applicationId, userId);
        if (myPosition.equals(AppUserGroupAuth.Position.MANAGER)) {
            return true;
        }
        
        return false;
        
    }

    private boolean existManager(ApplicationConfiguration appConfig) {
        List<AppUserGroupAuth> appUserGroupAuthList = appConfig.getAppUserGroupAuth();
        
        for (AppUserGroupAuth auth : appUserGroupAuthList) {
            if (AppUserGroupAuth.Position.MANAGER.equals(auth.getPosition())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void updateAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth) {
        applicationConfigDao.updateAppUserGroupAuth(appUserGroupAuth);
    }

    @Override
    public void deleteAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth) {
        applicationConfigDao.deleteAppUserGroupAuth(appUserGroupAuth);
    }

    @Override
    public void insertAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth) {
        applicationConfigDao.insertAppUserGroupAuth(appUserGroupAuth);
    }
}
