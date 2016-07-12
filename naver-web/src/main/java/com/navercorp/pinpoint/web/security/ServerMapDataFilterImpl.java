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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author minwoo.jung
 */
public class ServerMapDataFilterImpl extends AppConfigOrganizer implements ServerMapDataFilter {

    @Autowired
    ApplicationConfigService applicationConfigService;
    
    @Override
    public boolean filter(Application application) {
        return isAuthorized(application) ? false : true;
    }
    
    private boolean isAuthorized(Application application) {
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) { 
            return false;
        }
        if (isPinpointManager(authentication)) {
            return true;
        }
        
        String applicationId = application.getName();
        List<AppUserGroupAuth> userGroupAuths = userGroupAuth(authentication, applicationId);
        
        for(AppUserGroupAuth auth : userGroupAuths) {
            if (auth.getAppAuthConfiguration().getServerMapData() == false) {
                return true;
            }
        }
        
        return false;
    }

}
