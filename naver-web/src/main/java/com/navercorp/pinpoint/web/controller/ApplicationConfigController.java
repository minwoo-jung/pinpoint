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

package com.navercorp.pinpoint.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/application/userGroupAuth"})
public class ApplicationConfigController {
    
    private static final String APPLICATION_ID = "applicationId";
    private static final String USER_GROUP_ID = "userGroupId";
    private static final String USER_ID = "userId";
    private static final String ROLE = "role";
    private static final String MY_AUTHORITY = "myAuthority";
    private static final String CONFIGURATION = "configuration";
    private static final String USER_GROUP_AUTH_LIST = "userGroupAuthList";
    
    @Autowired
    ApplicationConfigService appConfigService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroup(@RequestBody Map<Object, Object> params) {
        Map<String, String> result = new HashMap<>();
        boolean isvalid = ValidationCheck(params);
        
        if (isvalid == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/userGroupId/role/configuration. params value : " + params);
            return result;
        }
        
        String userId = (String) params.get(USER_ID);
        String applicationId = (String) params.get(APPLICATION_ID);
        AppUserGroupAuth appUserGroupAuth = createAppUserGroupAuth(params);

        if (canInsertConfiguration(appUserGroupAuth, userId) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "user can not edit configuration . params value : " + params);
            return result;
        }
        
        appConfigService.insertAppUserGroupAuth(appUserGroupAuth);
        Role role = appConfigService.searchMyRole(applicationId, userId);

        result.put("result", "SUCCESS");
        result.put(MY_AUTHORITY, role.toString());
        return result;
    }

    private boolean canInsertConfiguration(AppUserGroupAuth appUserGroupAuth, String userId) {
        return appConfigService.canInsertConfiguration(appUserGroupAuth, userId);
    }

    private AppUserGroupAuth createAppUserGroupAuth(Map<Object, Object> params) {
        AppAuthConfiguration configuration = createInstanceFromMap(params.get(CONFIGURATION));
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth((String)params.get(APPLICATION_ID), (String)params.get(USER_GROUP_ID), (String)params.get(ROLE), configuration);
        return appUserGroupAuth;
    }
    
    private AppAuthConfiguration createInstanceFromMap(Object params) {
        Map<Object, Object> configMap = (Map<Object, Object>)params; 
        
        AppAuthConfiguration configuration = new AppAuthConfiguration();
        if (configMap.get("apiMetaData") != null) {
            configuration.setApiMetaData((Boolean)configMap.get("apiMetaData"));
        }
        if (configMap.get("sqlMetaData") != null) {
            configuration.setApiMetaData((Boolean)configMap.get("apiMetaData"));
        }
        if (configMap.get("paramMetaData") != null) {
            configuration.setApiMetaData((Boolean)configMap.get("paramMetaData"));
        }
        if (configMap.get("serverMapData") != null) {
            configuration.setApiMetaData((Boolean)configMap.get("serverMapData"));
        }
        
        return configuration;
    }

    private boolean ValidationCheck(Map<Object, Object> params) {
        if (StringUtils.isEmpty(params.get(APPLICATION_ID)) || StringUtils.isEmpty(params.get(USER_GROUP_ID)) || StringUtils.isEmpty(params.get(ROLE))) {
            return false;
        }
        
        if (validationCheckConfiguration(params.get(CONFIGURATION)) == false){
            return false;
        }
        
        return true;
    }
    
    private boolean validationCheckConfiguration(Object config) {
        if (!(config instanceof Map)) {
            return false;
        }
        Map<Object, Object> configuration = (Map<Object, Object>)config; 
        
        for(Map.Entry<Object, Object> entry : configuration.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                return false;
            }
            if (!(entry.getValue() instanceof Boolean)) {
                return false;
            }
        }
        
        return true;
    }


    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody Map<Object, Object> params) {
        String applicationId = (String) params.get(APPLICATION_ID);
        String userGroupId = (String) params.get(USER_GROUP_ID);
        String userId = (String) params.get(USER_ID);
        
        
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(userGroupId) || StringUtils.isEmpty(userId)) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not applicationId/userGroupId/userId.");
            return result;
        }
        
        if(canEditConfiguration(params) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "User can not edit configuration. params value : " + params);
            return result;
        }
        
        if(Role.GUEST.getName().equals(userGroupId)) {
            result.put("errorCode", "500");
            result.put("errorMessage", "You can't delete guest userGroup. params value : " + params);
            return result;
        }
        
        appConfigService.deleteAppUserGroupAuth(new AppUserGroupAuth(applicationId, userGroupId, "", null));
        Role role = appConfigService.searchMyRole(applicationId, userId);
        
        result.put("result", "SUCCESS");
        result.put(MY_AUTHORITY, role.toString());
        return result;
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroup(@RequestBody Map<Object, Object> params) {
        Map<String, String> result = new HashMap<>();
        boolean isvalid = ValidationCheck(params);
        
        if (isvalid == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not applicationId/userGroupId/role/configuration. params value : " + params);
            return result;
        }
        if (canEditConfiguration(params) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "User can not edit configuration . params value : " + params);
            return result;
        }
        
        AppUserGroupAuth appUserGroupAuth = createAppUserGroupAuth(params);
        appConfigService.updateAppUserGroupAuth(appUserGroupAuth);
        Role role = appConfigService.searchMyRole((String)params.get(APPLICATION_ID), (String)params.get(USER_ID));

        result.put("result", "SUCCESS");
        result.put(MY_AUTHORITY, role.toString());
        return result;
    }
    
    private boolean canEditConfiguration(Map<Object, Object> params) {
        return appConfigService.canEditConfiguration((String)params.get(APPLICATION_ID), (String)params.get(USER_ID));
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getUserGroup(@RequestParam(APPLICATION_ID) String applicationId, @RequestParam(USER_ID) String userId) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(applicationId)) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not userId/applicationId.");
            return result;
        }
        
        ApplicationConfiguration appConfig = appConfigService.selectApplicationConfiguration(applicationId);
        Role role = appConfigService.searchMyRole(applicationId, userId);
        result.put(MY_AUTHORITY, role.toString());
        result.put(USER_GROUP_AUTH_LIST, appConfig.getAppUserGroupAuth());
        
        return result;
    }
}
