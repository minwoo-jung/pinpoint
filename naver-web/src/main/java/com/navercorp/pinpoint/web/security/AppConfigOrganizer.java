package com.navercorp.pinpoint.web.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;

public class AppConfigOrganizer {
    @Autowired
    ApplicationConfigService applicationConfigService;
    
    @Autowired
    UserGroupService userGroupService;
    
    protected ApplicationConfiguration getApplicationConfiguration(PinpointAuthentication authentication, String applicationId) {
        ApplicationConfiguration appConfig = authentication.getApplicationConfiguration(applicationId);
        
        if (appConfig == null) {
            appConfig = applicationConfigService.selectApplicationConfiguration(applicationId);
            authentication.addApplicationConfiguration(appConfig);
        }
        
        return appConfig;
    }
    protected List<AppUserGroupAuth> userGroupAuth(PinpointAuthentication authentication, String applicationId) {
        ApplicationConfiguration appConfig = getApplicationConfiguration(authentication, applicationId);
        
        Map<String, AppUserGroupAuth> appUserGroupAuthes = appConfig.getAppUserGroupAuthes();
        List<UserGroup> userGroupList = userGroupService.selectUserGroupByUserId(authentication.getPrincipal());
        
        List<AppUserGroupAuth> containedUserGroups = new ArrayList<>();
        
        for(UserGroup userGroup : userGroupList) {
            if (appUserGroupAuthes.containsKey(userGroup.getId())) {
                containedUserGroups.add(appUserGroupAuthes.get(userGroup.getId()));
            }
        }
        
        return containedUserGroups;
    }
    
    protected boolean isEmptyUserGroup(PinpointAuthentication authentication, String applicationId) {
        ApplicationConfiguration appConfig = getApplicationConfiguration(authentication, applicationId);
        
        if (appConfig.getAppUserGroupAuthes().size() == 0) {
            return true;
        }
        
        return false;
    }

    protected boolean isPinpointManager(PinpointAuthentication authentication) {
        if (authentication.isPinpointManager()) {
            return true;
        }
        return false;
    }
}
