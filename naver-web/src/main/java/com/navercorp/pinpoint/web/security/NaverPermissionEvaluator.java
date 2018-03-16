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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentParam;
import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author minwoo-jung
 */
public class NaverPermissionEvaluator extends AppConfigOrganizer implements PermissionEvaluator {

    public static final String ADMIN = "admin";
    public static final String INSPECTOR = "inspector";
    public static final String APPLICATION = "application";
    public static final String AGENT_PARAM = "agentParam";
    
    @Autowired
    ServerMapDataFilter serverMapDataFilter;
    
    @Autowired
    AgentInfoService agentInfoService;

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

        if (ADMIN.equals(permission)) {
            return pinAuth.isPinpointManager();
        }
        if (INSPECTOR.equals(permission)) {
            return hasPermissionForAppAuth(target, targetType, (String)permission);
        }
        
        return false;
    }

    private boolean hasPermissionForAppAuth(Serializable target, String targetType, String permission) {
        if (APPLICATION.equals(targetType)) {
            String applicationId = (String)target;
            boolean filtered = serverMapDataFilter.filter(new Application(applicationId, ServiceType.UNKNOWN));
            return !filtered;
        } else if (AGENT_PARAM.equals(targetType)) {
            AgentParam agentParam = (AgentParam)target;
            AgentInfo agentInfo = agentInfoService.getAgentInfo(agentParam.getAgentId(), agentParam.getTimeStamp());
            boolean filtered = serverMapDataFilter.filter(new Application(agentInfo.getApplicationName(), ServiceType.UNKNOWN));
            return !filtered;
        }
        
        return false;
    }
}
