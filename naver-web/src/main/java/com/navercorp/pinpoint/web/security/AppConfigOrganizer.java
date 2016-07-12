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
    

    protected List<AppUserGroupAuth> userGroupAuth(PinpointAuthentication authentication, String applicationId) {
        ApplicationConfiguration appConfig = authentication.getApplicationConfiguration(applicationId);
        
        if (appConfig == null) {
            appConfig = applicationConfigService.selectApplicationConfiguration(applicationId);
            authentication.addApplicationConfiguration(appConfig);
        }
        
        Map<String, AppUserGroupAuth> appUserGroupAuthes = appConfig.getAppUserGroupAuthes();
        List<UserGroup> userGroupList = userGroupService.selectUserGroupByUserId(authentication.getPrincipal());
        
        List<AppUserGroupAuth> containedUserGroups = new ArrayList<>();
        
        for(UserGroup userGroup : userGroupList) {
            if (appUserGroupAuthes.containsKey(userGroup.getId())) {
                containedUserGroups.add(appUserGroupAuthes.get(userGroup.getId()));
            }
        }
        
        if (containedUserGroups.size() == 0) {
            containedUserGroups.add(appUserGroupAuthes.get(Role.GUEST.getName()));
        }
        
        return containedUserGroups;
    }

    protected boolean isPinpointManager(PinpointAuthentication authentication) {
        if (authentication.isPinpointManager()) {
            return true;
        }
        return false;
    }
}
