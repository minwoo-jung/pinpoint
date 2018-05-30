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
package com.navercorp.pinpoint.manager.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author minwoo.jung
 */
public class LocalAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {
        final String userId = String.valueOf(auth.getPrincipal());

        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication(userId, userId);
        if (pinpointAuthentication.getPrincipal().isEmpty() == false) {
            pinpointAuthentication.addAuthority(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return pinpointAuthentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

}