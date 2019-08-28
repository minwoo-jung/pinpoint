/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.security.internal;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JwtCookieCreaterTest {

    @Test(expected = InternalAuthenticationServiceException.class)
    public void createJwtCookieTest() {
        JwtCookieCreater.createJwtCookie("key");
    }

    @Test(expected = InternalAuthenticationServiceException.class)
    public void createJwtCookieTest2() {
        SecurityContextHolder.getContext().setAuthentication(new PinpointAuthentication());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        try {
            JwtCookieCreater.createJwtCookie("key");
        } finally {
            SecurityContextHolder.clearContext();
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void createJwtCookieTest3() {
        SecurityContextHolder.getContext().setAuthentication(new PinpointAuthentication());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(new MockHttpServletRequest());
        servletRequestAttributes.setAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, new PaaSOrganizationInfo(), RequestAttributes.SCOPE_REQUEST);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        try {
            Cookie key = JwtCookieCreater.createJwtCookie("encodingKey");
            assertEquals(key.getName(), "jwt");
        } finally {
            SecurityContextHolder.clearContext();
            RequestContextHolder.resetRequestAttributes();
        }
    }
}