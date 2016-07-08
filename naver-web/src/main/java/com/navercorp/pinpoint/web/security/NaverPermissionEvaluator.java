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

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo-jung
 */
public class NaverPermissionEvaluator implements PermissionEvaluator {
    
    public static final String INSPECTOR = "inspector";
    public static final String APPLICATION = "application";
    
    @Autowired
    ApplicationConfigService applicationConfigService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable target, String targetType, Object permission) {
        final PinpointAuthentication pinAuth = (PinpointAuthentication) authentication;
        
        if (pinAuth.isPinpointManager()) {
            return true;
        }
        
        if (INSPECTOR.equals(permission)) {
            return hasPermissionForAppAuth(pinAuth, target, targetType, (String)permission);
        }
        return false;
    }

    private boolean hasPermissionForAppAuth(PinpointAuthentication pinAuth, Serializable target, String targetType, String permission) {
        
        if (APPLICATION.equals(targetType)) {
            String applicationId = (String)target;
            
            ApplicationConfiguration appConfig = pinAuth.getApplicationConfiguration(applicationId);
            
            if (appConfig == null) {
                appConfig = applicationConfigService.selectApplicationConfiguration(applicationId);
                pinAuth.addApplicationConfiguration(appConfig);
            }
            
            AppAuthConfiguration appAuthConfig = null;
            //TODO : 개선 필요
//            AppAuthConfiguration appAuthConfig = appConfig.getAppAuthConfiguration();
            
//            if (INSPECTOR.equals(permission) && !appAuthConfig.getInspector()) {
//                return true;
//            }
            if (appConfig.isAffiliatedAppUserGroup(pinAuth.getUserGroupList())) {
                return true;
            }
        }
        
        return false;
    }
}
