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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
public class LocalAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserService userService;

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    ApplicationConfigService configService;

    @Override
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {
        final String userId = String.valueOf(auth.getPrincipal());

        StaticOrganizationInfoAllocator.allocateForSessionScope(userId);

        User user = userService.selectUserByUserId(userId);
        List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userId);
        boolean pinpointManager = isManager(userId);
        PinpointAuthentication authentication;

        if (user != null) {
            authentication = new PinpointAuthentication(user.getUserId(), user.getName(), userGroups, null, true, pinpointManager) {
                @Override
                public ApplicationConfiguration getApplicationConfiguration(String applicationId) {
                    return null;
                }

                @Override
                public void addApplicationConfiguration(ApplicationConfiguration appConfig) {
                }
            };
            GrantedAuthority grantedAuthority = new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_USER";
                }
            };
            List authorities = new ArrayList<GrantedAuthority>(1);
            authorities.add(grantedAuthority);
            authentication.setAuthorities(authorities);
        } else {
            authentication = new PinpointAuthentication() {
                @Override
                public ApplicationConfiguration getApplicationConfiguration(String applicationId) {
                    return null;
                }

                @Override
                public void addApplicationConfiguration(ApplicationConfiguration appConfig) {
                }
            };
        }

        return authentication;
    }

    private boolean isManager(String userId) {
        List<User> user = configService.selectManagerByUserId(userId);

        if (user.size() > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

}
