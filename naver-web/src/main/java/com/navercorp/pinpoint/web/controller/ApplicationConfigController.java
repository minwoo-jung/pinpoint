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
import com.navercorp.pinpoint.web.vo.AppUserGroupAuthParam;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/application/userGroupAuth"})
public class ApplicationConfigController {
    
    private static final String APPLICATION_ID = "applicationId";
    private static final String USER_ID = "userId";
    private static final String MY_ROLE = "myRole";
    private static final String USER_GROUP_AUTH_LIST = "userGroupAuthList";
    
    @Autowired
    ApplicationConfigService appConfigService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroup(@RequestBody AppUserGroupAuthParam params) {
        Map<String, String> result = new HashMap<>();
        boolean isvalid = ValidationCheck(params);
        
        if (isvalid == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/userGroupId/role/configuration. params value : " + params);
            return result;
        }

        if (canInsertConfiguration(params) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "user can not edit configuration . params value : " + params);
            return result;
        }
        
        appConfigService.insertAppUserGroupAuth(params);
        Role role = appConfigService.searchMyRole(params.getApplicationId(), params.getUserId());

        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }

    private boolean canInsertConfiguration(AppUserGroupAuthParam params) {
        return appConfigService.canInsertConfiguration(params, params.getUserId());
    }

    private boolean ValidationCheck(AppUserGroupAuthParam params) {
        if (StringUtils.isEmpty(params.getApplicationId()) || StringUtils.isEmpty(params.getUserGroupId()) || (params.getRole() == null)) {
            return false;
        }

        return true;
    }


    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody AppUserGroupAuthParam params) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isEmpty(params.getApplicationId()) || StringUtils.isEmpty(params.getUserGroupId()) || StringUtils.isEmpty(params.getUserId())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not applicationId/userGroupId/userId.");
            return result;
        }
        
        if(canEditConfiguration(params) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "User can not edit configuration. params value : " + params);
            return result;
        }
        
        if(Role.GUEST.getName().equals(params.getUserGroupId())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "You can't delete guest userGroup. params value : " + params);
            return result;
        }
        
        appConfigService.deleteAppUserGroupAuth(params);
        Role role = appConfigService.searchMyRole(params.getApplicationId(), params.getUserId());
        
        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroup(@RequestBody AppUserGroupAuthParam params) {
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
        
        appConfigService.updateAppUserGroupAuth(params);
        Role role = appConfigService.searchMyRole(params.getApplicationId(), params.getUserId());

        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }
    
    private boolean canEditConfiguration(AppUserGroupAuthParam params) {
        return appConfigService.canEditConfiguration(params.getApplicationId(), params.getUserId());
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
        
        appConfigService.initApplicationConfiguration(applicationId);
        ApplicationConfiguration appConfig = appConfigService.selectApplicationConfiguration(applicationId);
        Role role = appConfigService.searchMyRole(applicationId, userId);
        result.put(MY_ROLE, role.toString());
        result.put(USER_GROUP_AUTH_LIST, appConfig.getAppUserGroupAuth());
        
        return result;
    }
}
