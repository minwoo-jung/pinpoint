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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.service.UserConfigService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.ApplicationModel;
import com.navercorp.pinpoint.web.vo.InspectorChart;
import com.navercorp.pinpoint.web.vo.UserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/userConfiguration"})
public class UserConfigController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String FAVORITE_APPLICATIONS = "favoriteApplications";
    private static final String APPLICATION_INSPECTOR_CHARTS = "applicationInspectorCharts";
    private static final String AGENT_INSPECTOR_CHARTS = "agentInspectorCharts";

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private UserService userService;

//    @Deprecated //this api is only used form version 1 UI
//    @RequestMapping(method = RequestMethod.GET)
//    @ResponseBody
//    public  Map<String, List<ApplicationModel>> getUserConfiguration() {
//        List<ApplicationModel> favoriteApplications = userConfigService.selectFavoriteApplications(getUserId());
//
//        Map<String, List<ApplicationModel>> result = new HashMap<>();
//        result.put(FAVORITE_APPLICATIONS, favoriteApplications);
//        return result;
//    }
//
//    @Deprecated //this api is only used form version 1 UI
//    @RequestMapping(method = RequestMethod.PUT)
//    @ResponseBody
//    public Map<String, String> updateUserConfiguration(@RequestBody UserConfiguration userConfiguration) {
//        userConfiguration.setUserId(getUserId());
//        userConfigService.updateFavoriteApplications(userConfiguration);
//
//        Map<String, String> result = new HashMap<>();
//        result.put("result", "SUCCESS");
//        return result;
//    }

    @RequestMapping(value = "/favoriteApplications", method = RequestMethod.GET)
    @ResponseBody
    public  Map<String, List<ApplicationModel>> getFavoriteApplications() {
        List<ApplicationModel> favoriteApplications = userConfigService.selectFavoriteApplications(getUserId());

        Map<String, List<ApplicationModel>> result = new HashMap<>();
        result.put(FAVORITE_APPLICATIONS, favoriteApplications);
        return result;
    }

    @RequestMapping(value = "/favoriteApplications", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateFavoriteApplications(@RequestBody UserConfiguration userConfiguration) {
        userConfiguration.setUserId(getUserId());
        userConfigService.updateFavoriteApplications(userConfiguration);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(value = "/inspectorChart/application", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<InspectorChart>> getApplicationInspectorCharts() {
        List<InspectorChart> applicationInspectorChartList = userConfigService.selectApplicationInspectorCharts(getUserId());

        Map<String, List<InspectorChart>> result = new HashMap<>();
        result.put(APPLICATION_INSPECTOR_CHARTS, applicationInspectorChartList);
        return result;
    }

    @RequestMapping(value = "/inspectorChart/application", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateApplicationInspectorCharts(@RequestBody UserConfiguration userConfiguration) {
        userConfiguration.setUserId(getUserId());
        userConfigService.updateApplicationInspectorCharts(userConfiguration);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(value = "/inspectorChart/agent", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<InspectorChart>> getAgentInspectorChart() {
        List<InspectorChart> agentInspectorChartList = userConfigService.selectAgentInspectorCharts(getUserId());

        Map<String, List<InspectorChart>> result = new HashMap<>();
        result.put(AGENT_INSPECTOR_CHARTS, agentInspectorChartList);
        return result;
    }

    @RequestMapping(value = "/inspectorChart/agent", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateAgentInspectorChart(@RequestBody UserConfiguration userConfiguration) {
        userConfiguration.setUserId(getUserId());
        userConfigService.updateAgentInspectorCharts(userConfiguration);

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

    private String getUserId() {
        String userId = userService.getUserIdFromSecurity();

        if (StringUtils.isEmpty(userId)) {
            throw new RuntimeException("userId is empty.");
        }

        return userId;
    }
}
