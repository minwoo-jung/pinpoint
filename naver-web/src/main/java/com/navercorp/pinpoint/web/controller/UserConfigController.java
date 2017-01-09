/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.web.dao.UserConfigDao;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.UserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/userConfiguration"})
public class UserConfigController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SSO_USER = "SSO_USER";

    @Autowired
    private UserConfigDao userConfigDao;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserConfiguration(@RequestBody UserConfiguration userConfiguration, @RequestHeader(SSO_USER) String userId) {
        userConfiguration.setUserId(userId);
        userConfigDao.insertUserConfiguration(userConfiguration);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public  UserConfiguration getUserConfiguration(@RequestHeader(SSO_USER) String userId) {
        return userConfigDao.selectUserConfiguration(userId);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserConfiguration(@RequestHeader(SSO_USER) String userId) {
        userConfigDao.deleteUserConfiguration(userId);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserConfiguration(@RequestBody UserConfiguration userConfiguration, @RequestHeader(SSO_USER) String userId) {
        userConfiguration.setUserId(userId);
        userConfigDao.updateUserConfiguration(userConfiguration);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> handleException(Exception e) {
        logger.error("Exception occurred while trying to CRUD User Configuration", e);
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Exception occurred while trying to CRUD User Configuration");
        return result;
    }
}
