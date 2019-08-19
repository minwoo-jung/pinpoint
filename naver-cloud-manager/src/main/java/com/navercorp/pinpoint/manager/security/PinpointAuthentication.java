/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.manager.security;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author minwoo.jung
 */
public class PinpointAuthentication implements Authentication {

    private static final String EMPTY_STRING = "";

    private final String userId;
    private final String name;
    private final Collection<GrantedAuthority> authorities;
    private boolean authenticated;

    public PinpointAuthentication(String userId, String name) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be empty");
        }

        this.userId = userId;
        this.name = name;
        this.authorities = new ArrayList<>(0);
    }

    public PinpointAuthentication() {
        userId = EMPTY_STRING;
        name = EMPTY_STRING;
        this.authorities = new ArrayList<>(0);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void addAuthority(GrantedAuthority authoritiy) {
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
}


