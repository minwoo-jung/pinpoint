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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
@Service
public class ApplicationConfigServiceImpl implements ApplicationConfigService {

    @Autowired
    ApplicationConfigDao applicationConfigDao;
    
    @Autowired
    UserGroupService userGroupService;
    
    //추후 옵션으로 빼야함. paas 형태로 제공할때 false가 필요함
    boolean anyOneOccupyAtfirst = true;
    
    @Override
    public ApplicationConfiguration selectApplicationConfiguration(String applicationId) {
        List<AppUserGroupAuth> appAuthUserGroupList = applicationConfigDao.selectAppUserGroupAuthList(applicationId);
        
        if(appAuthUserGroupList.size() == 0) {
            AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, Role.GUEST.toString(), Role.GUEST.toString(), new AppAuthConfiguration());
            applicationConfigDao.insertAppUserGroupAuth(appUserGroupAuth);
            appAuthUserGroupList.add(appUserGroupAuth);
        }
            
        return new ApplicationConfiguration(applicationId, appAuthUserGroupList);
    }

    @Override
    public Role searchMyRole(ApplicationConfiguration appConfig, String userId) {
        Map<String, AppUserGroupAuth> appUserGroupAuthes = appConfig.getAppUserGroupAuthes();
        Map<String, UserGroup> myUserGroups = getUserGroups(userId); 
        Role myRole = Role.GUEST; 
        
        for(Map.Entry<String, AppUserGroupAuth> entry : appUserGroupAuthes.entrySet()) {
            String userGroupId = entry.getKey();
            if (myUserGroups.containsKey(userGroupId)) {
                if (!myRole.isHigherOrEqualLevel(entry.getValue().getRole())){
                    myRole = entry.getValue().getRole();
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
    public boolean canEditConfiguration(String applicationId, String userId) {
        ApplicationConfiguration appConfig = selectApplicationConfiguration(applicationId);
        
        if (appConfig.getAppUserGroupAuth().size() == 0  && anyOneOccupyAtfirst) {
            return true;
        }

        Role myRole = searchMyRole(appConfig, userId);
        if(myRole.equals(Role.MANAGER)) {
            return true;
        }
   
        if (existManager(appConfig) == false) {
           return anyOneOccupyAtfirst; 
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
    
    

}
