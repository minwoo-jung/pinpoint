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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
public class PinpointAuthentication implements Authentication {

    private String userId;
    private String name;
    private String password;
    private List<UserGroup> affiliatedUserGroupList = new ArrayList<>(0);
    private Map<String, ApplicationConfiguration> appConfigCache = new HashMap<String, ApplicationConfiguration>();
    private boolean authenticated = false;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean PinpointManager = false;
    
    public PinpointAuthentication(String userId, String name, List<UserGroup> affiliatedUserGroupList, String password, boolean authenticated, boolean pinpointManager) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.affiliatedUserGroupList = affiliatedUserGroupList;
        this.authenticated = authenticated;
        this.PinpointManager = pinpointManager;
    }
    
    
    public PinpointAuthentication() {
    }

    public boolean isPinpointManager() {
        return PinpointManager;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getCredentials() {
        return password;
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
