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
package com.navercorp.pinpoint.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo.jung
 */
public class ServerMapDataFilterImpl implements ServerMapDataFilter {

    @Autowired
    ApplicationConfigService applicationConfigService;
    
    @Override
    public boolean filter(Application application) {
        return isAuthorized(application) ? false : true;
    }
    
    private boolean isAuthorized(Application application) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) { 
            return false;
        }
        
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isPinpointManager()) {
            return true;
        }
        
        String applicationId = application.getName();
        ApplicationConfiguration appConfig = authentication.getApplicationConfiguration(applicationId);
        
        if (appConfig == null) {
            appConfig = applicationConfigService.selectApplicationConfiguration(applicationId);
            authentication.addApplicationConfiguration(appConfig);
        }
        
        AppAuthConfiguration appAuthConfig = appConfig.getAppAuthConfiguration();
        
        if (!appAuthConfig.getServerMapData()) {
            return true;
        }
        if (appConfig.isAffiliatedAppUserGroup(authentication.getUserGroupList())) {
            return true;
        }
        
        return false;
    }

}
