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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/application/userGroupAuth"})
public class ApplicationConfigController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private static final String SSO_USER = "SSO_USER";
    private static final String APPLICATION_ID = "applicationId";
    private static final String USER_ID = "userId";
    private static final String MY_ROLE = "myRole";
    private static final String USER_GROUP_AUTH_LIST = "userGroupAuthList";
    
    @Autowired
    ApplicationConfigService appConfigService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroup(@RequestBody AppUserGroupAuth appUserGroupAuth, @RequestHeader(SSO_USER) String userId) {
        Map<String, String> result = new HashMap<>();
        boolean isvalid = ValidationCheck(appUserGroupAuth);
        
        if (isvalid == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/userGroupId/role/configuration. params value : " + appUserGroupAuth);
            return result;
        }

        if (canInsertConfiguration(appUserGroupAuth, userId) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "user can not edit configuration . params value : " + appUserGroupAuth);
            return result;
        }
        
        appConfigService.insertAppUserGroupAuth(appUserGroupAuth);
        Role role = appConfigService.searchMyRole(appUserGroupAuth.getApplicationId(), userId);

        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }

    private boolean canInsertConfiguration(AppUserGroupAuth appUserGroupAuth, String userId) {
        return appConfigService.canInsertConfiguration(appUserGroupAuth, userId);
    }

    private boolean ValidationCheck(AppUserGroupAuth appUserGroupAuth) {
        if (StringUtils.isEmpty(appUserGroupAuth.getApplicationId()) || StringUtils.isEmpty(appUserGroupAuth.getUserGroupId()) || (appUserGroupAuth.getRole() == null)) {
            return false;
        }

        return true;
    }


    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody AppUserGroupAuth appUserGroupAuth, @RequestHeader(SSO_USER) String userId) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isEmpty(appUserGroupAuth.getApplicationId()) || StringUtils.isEmpty(appUserGroupAuth.getUserGroupId()) || StringUtils.isEmpty(userId)) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not applicationId/userGroupId/userId.");
            return result;
        }
        
        if(canEditConfiguration(appUserGroupAuth, userId) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "User can not edit configuration. params value : " + appUserGroupAuth);
            return result;
        }
        
        if(Role.GUEST.getName().equals(appUserGroupAuth.getUserGroupId())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "You can't delete guest userGroup. params value : " + appUserGroupAuth);
            return result;
        }
        
        appConfigService.deleteAppUserGroupAuth(appUserGroupAuth);
        Role role = appConfigService.searchMyRole(appUserGroupAuth.getApplicationId(), userId);
        
        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroup(@RequestBody AppUserGroupAuth appUserGroupAuth, @RequestHeader(SSO_USER) String userId) {
        Map<String, String> result = new HashMap<>();
        boolean isvalid = ValidationCheck(appUserGroupAuth);
        
        if (isvalid == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "There is not applicationId/userGroupId/role/configuration. params value : " + appUserGroupAuth);
            return result;
        }
        if (canEditConfiguration(appUserGroupAuth, userId) == false) {
            result.put("errorCode", "500");
            result.put("errorMessage", "User can not edit configuration . params value : " + appUserGroupAuth);
            return result;
        }
        
        appConfigService.updateAppUserGroupAuth(appUserGroupAuth);
        Role role = appConfigService.searchMyRole(appUserGroupAuth.getApplicationId(), userId);

        result.put("result", "SUCCESS");
        result.put(MY_ROLE, role.toString());
        return result;
    }
    
    private boolean canEditConfiguration(AppUserGroupAuth appUserGroupAuth, String userId) {
        return appConfigService.canEditConfiguration(appUserGroupAuth.getApplicationId(), userId);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getUserGroup(@RequestParam(APPLICATION_ID) String applicationId, @RequestParam(USER_ID) String userId, @RequestHeader(SSO_USER) String headerUserId) {
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
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> handleException(Exception e) {
        logger.error("Exception occurred while trying to CRUD application configuration.", e);
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Exception occurred while trying to CRUD application configuration.");
        return result;
    }
}
