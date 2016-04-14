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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.ApplicationAuthority;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/application/userGroup"})
public class ApplicationConfigController {
    
    private static final String APPLICATION_ID = "applicationId";
    
    @Autowired
    ApplicationConfigDao appConfigDao;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroup(@RequestBody ApplicationAuthority appAuth) {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isEmpty(appAuth.getApplicationId()) || StringUtils.isEmpty(appAuth.getUserGroupId()) || appAuth.getAuthority() == null) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/userGroupId/authority to insert alarm rule");
            return result;
        }
        
        appConfigDao.insertAuthority(appAuth);

        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody ApplicationAuthority appAuth) {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isEmpty(appAuth.getApplicationId()) || StringUtils.isEmpty(appAuth.getUserGroupId())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationIduserGroupId/authority to insert alarm rule");
            return result;
        }
        
        appConfigDao.deleteAuthority(appAuth);

        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroup(@RequestBody ApplicationAuthority appAuth) {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isEmpty(appAuth.getApplicationId()) || StringUtils.isEmpty(appAuth.getUserGroupId()) || appAuth.getAuthority() == null) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/userGroupId/authority to insert alarm rule");
            return result;
        }
        
        appConfigDao.updateAuthority(appAuth);

        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ApplicationAuthority> getUserGroup(@RequestParam(APPLICATION_ID) String applicationId) {
        return appConfigDao.selectAuthority(applicationId);
    }
}
