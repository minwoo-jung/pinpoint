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
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;
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
    
    //추후 옵션으로 빼야함. paas 형태로 제공할때 false가 필요함
    boolean anyOneOccupyAtfirst = true;
    
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
            AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, Role.GUEST.toString(), Role.GUEST.toString(), new AppAuthConfiguration());
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
    public Role searchMyRole(String applicationId, String userId) {
        ApplicationConfiguration appConfig = selectApplicationConfiguration(applicationId);
        Map<String, AppUserGroupAuth> appUserGroupAuthes = appConfig.getAppUserGroupAuthes();
        Map<String, UserGroup> myUserGroups = getUserGroups(userId); 
        Role myRole = Role.GUEST; 
        
        for(Map.Entry<String, AppUserGroupAuth> entry : appUserGroupAuthes.entrySet()) {
            String userGroupId = entry.getKey();
            if (myUserGroups.containsKey(userGroupId)) {
                Role role = entry.getValue().getRole();
                if (role != null && role.isHigherOrEqualLevel(myRole)){
                    myRole = role;
                }
            }
        }
        
        return myRole;
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
    @Transactional(readOnly = true)
    public boolean canInsertConfiguration(AppUserGroupAuth appUserGroupAuth, String userId) {
        ApplicationConfiguration appConfig = selectApplicationConfiguration(appUserGroupAuth.getApplicationId());
        
        if (appConfig.getAppUserGroupAuth().isEmpty() || existManager(appConfig) == false) {
            Map<String, UserGroup> userGroups = getUserGroups(userId);
            if (userGroups.containsKey(appUserGroupAuth.getUserGroupId()) && Role.MANAGER.equals(appUserGroupAuth.getRole())) {
                return anyOneOccupyAtfirst; 
            } else {
                return false;
            }
        }
        if (canEditConfiguration(appUserGroupAuth.getApplicationId(), userId)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canEditConfiguration(String applicationId, String userId) {
        Role myRole = searchMyRole(applicationId, userId);
        if (myRole.equals(Role.MANAGER)) {
            return true;
        }
        
        return false;
        
    }

    private boolean existManager(ApplicationConfiguration appConfig) {
        List<AppUserGroupAuth> appUserGroupAuthList = appConfig.getAppUserGroupAuth();
        
        for (AppUserGroupAuth auth : appUserGroupAuthList) {
            if (Role.MANAGER.equals(auth.getRole())) {
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
