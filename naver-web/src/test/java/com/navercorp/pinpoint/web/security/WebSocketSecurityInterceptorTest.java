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

package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.role.PermissionCollection;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class WebSocketSecurityInterceptorTest {

    @Test
    public void beforeHandshakeTest() throws Exception {
        WebSocketSecurityInterceptor interceptor = new WebSocketSecurityInterceptor();

        final String userId = "KR0000";
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("SSO_USER", userId);
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        UserService userService = mock(UserService.class);
        when(userService.selectUserByUserId(userId)).thenReturn(new User(userId, "name", "department", "phoneNumber", "email"));
        ReflectionTestUtils.setField(interceptor, "userService", userService);
        UserGroupService userGroupService = mock(UserGroupService.class);
        when(userGroupService.selectUserGroupByUserId(userId)).thenReturn(Collections.EMPTY_LIST);
        ReflectionTestUtils.setField(interceptor, "userGroupService", userGroupService);
        ApplicationConfigService configService = mock(ApplicationConfigService.class);
        when(configService.isManager(userId)).thenReturn(true);
        ReflectionTestUtils.setField(interceptor, "configService", configService);
        RoleService roleService = mock(RoleService.class);
        RoleInformation roleInformation = new RoleInformation("roleId", PermissionCollection.DEFAULT);
        when(roleService.getUserPermission(userId)).thenReturn(roleInformation);
        ReflectionTestUtils.setField(interceptor, "roleService", roleService);


        assertTrue(interceptor.beforeHandshake(request, response, null, null));

        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        assertEquals(authentication.getPrincipal(), userId);

        interceptor.afterHandshake(request, response, null, null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void beforeHandshake2Test() throws Exception {
        WebSocketSecurityInterceptor interceptor = new WebSocketSecurityInterceptor();

        final String userId = "KR0000";
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("SSO_USER", userId);
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        UserService userService = mock(UserService.class);
        when(userService.selectUserByUserId(userId)).thenReturn(null);
        ReflectionTestUtils.setField(interceptor, "userService", userService);

        assertTrue(interceptor.beforeHandshake(request, response, null, null));

        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        assertEquals(authentication.getPrincipal(), "");

        interceptor.afterHandshake(request, response, null, null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}