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
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author minwoo-jung
 */
public class NaverPermissionEvaluator implements PermissionEvaluator {

    public static final String INSPECTOR = "inspector";
    public static final String APPLICATION = "application";
    public static final String AGENT_PARAM = "agentParam";
    private static final EmptyObject EMPTY_OBJECT = new EmptyObject();
    private static final String EMPTY_STRING = "";

    @Autowired
    ServerMapDataFilter serverMapDataFilter;
    
    @Autowired
    AgentInfoService agentInfoService;

    @Autowired
    PermissionChecker permissionChecker;

    public boolean hasPermission(String permissionCollection) {
        return hasPermission(permissionCollection, EMPTY_OBJECT);
    }

    public boolean hasPermission(String permissionCollection, Serializable parameter) {
        return hasPermission(SecurityContextHolder.getContext().getAuthentication(), parameter, EMPTY_STRING, permissionCollection);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable parameter, String targetType, Object permissionOrAuthorizationType) {
        final PinpointAuthentication pinAuth = (PinpointAuthentication) authentication;

        final String permissionType = (String)permissionOrAuthorizationType;
        if (permissionType.startsWith(PermissionChecker.PREFIX_PERMISSION)) {
            return permissionChecker.checkPermission(pinAuth, permissionType, parameter);
        }

        final String authorizationType = (String)permissionOrAuthorizationType;
        if (INSPECTOR.equals(authorizationType)) {
            return hasAppAuthorization(pinAuth, parameter, targetType);
        }
        
        return false;
    }

    private boolean hasAppAuthorization(PinpointAuthentication pinAuth, Serializable parameter, String parameterType) {
        if (pinAuth.isObtainAllAuthorization()) {
            return true;
        }

        if (APPLICATION.equals(parameterType)) {
            String applicationId = (String)parameter;
            boolean filtered = serverMapDataFilter.filter(new Application(applicationId, ServiceType.UNKNOWN));
            return !filtered;
        } else if (AGENT_PARAM.equals(parameterType)) {
            AgentParam agentParam = (AgentParam)parameter;
            AgentInfo agentInfo = agentInfoService.getAgentInfo(agentParam.getAgentId(), agentParam.getTimeStamp());
            boolean filtered = serverMapDataFilter.filter(new Application(agentInfo.getApplicationName(), ServiceType.UNKNOWN));
            return !filtered;
        }
        
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        throw new UnsupportedOperationException();
    }

    private static class EmptyObject implements Serializable {
    }
}
