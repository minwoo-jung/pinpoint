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

import java.util.*;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;
import org.springframework.util.Assert;

/**
 * @author minwoo.jung
 */
public class PinpointAuthentication implements Authentication {

    private static final String EMPTY_STRING = "";

    private final String userId;
    private final String name;
    private final List<UserGroup> affiliatedUserGroupList;
    private final Map<String, ApplicationConfiguration> appConfigCache;
    private final Collection<GrantedAuthority> authorities;
    private final boolean PinpointManager;
    private final RoleInformation roleInformation;
    private boolean authenticated;

    public PinpointAuthentication(String userId, String name, List<UserGroup> affiliatedUserGroupList, boolean authenticated, boolean pinpointManager, RoleInformation roleInformation) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be empty");
        }
        Objects.requireNonNull(affiliatedUserGroupList, "affiliatedUserGroupList must not be empty");
        Objects.requireNonNull(roleInformation, "roleInformation must not be empty");

        this.userId = userId;
        this.name = name;
        this.affiliatedUserGroupList = affiliatedUserGroupList;
        this.authenticated = authenticated;
        this.PinpointManager = pinpointManager;
        this.authorities = new ArrayList<>(0);
        this.appConfigCache = new HashMap<String, ApplicationConfiguration>();
        this.roleInformation = roleInformation;
    }

    public PinpointAuthentication() {
        userId = EMPTY_STRING;
        name = EMPTY_STRING;
        affiliatedUserGroupList = new ArrayList<>(0);
        appConfigCache = new HashMap<String, ApplicationConfiguration>();
        authenticated = false;
        authorities = new ArrayList<>(0);
        PinpointManager = false;
        roleInformation = RoleInformation.UNASSIGNED_ROLE;
    }

    public boolean isPinpointManager() {
        return PinpointManager;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void addAuthority(GrantedAuthority authoritiy)
    {
        this.authorities.add(authoritiy);
    }

    @Override
    public String getCredentials() {
        return EMPTY_STRING;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public String getPrincipal() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    public RoleInformation getRoleInformation() {
        return roleInformation;
    }

    public ApplicationConfiguration getApplicationConfiguration(String applicationId) {
        return appConfigCache.get(applicationId);
    }

    public void addApplicationConfiguration(ApplicationConfiguration appConfig) {
        appConfigCache.put(appConfig.getApplicationId(), appConfig);
    }

    public List<UserGroup> getUserGroupList() {
        return this.affiliatedUserGroupList;
    }

}
