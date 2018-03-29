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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class SecurityServiceImplTest {

    @Test
    public void createPinpointAuthenticationTest() {
        final SecurityServiceImpl securityService = new SecurityServiceImpl();
        final String userId = "test_userId";

        UserService userService = mock(UserService.class);
        final User user = new User(userId, "name", "userdepartment", "010", "email@email.com");
        when((userService.selectUserByUserId(userId))).thenReturn(user);
        ReflectionTestUtils.setField(securityService, "userService", userService);

        UserGroupService userGroupService = mock(UserGroupService.class);
        List<UserGroup> userGroupList = new ArrayList<>();
        userGroupList.add(new UserGroup("1", "userGroup1"));
        userGroupList.add(new UserGroup("2", "userGroup2"));
        when(userGroupService.selectUserGroupByUserId(userId)).thenReturn(userGroupList);
        ReflectionTestUtils.setField(securityService, "userGroupService", userGroupService);

        ApplicationConfigService configService = mock(ApplicationConfigService.class);
        when(configService.isManager(userId)).thenReturn(true);
        ReflectionTestUtils.setField(securityService, "configService", configService);

        PinpointAuthentication pinpointAuthentication = securityService.createPinpointAuthentication(userId);

        assertNotNull(pinpointAuthentication);
        assertEquals(pinpointAuthentication.getPrincipal(), userId);
        assertEquals(pinpointAuthentication.getName(), user.getName());
        assertEquals(pinpointAuthentication.getUserGroupList(), userGroupList);
        assertTrue(pinpointAuthentication.isPinpointManager());
        assertTrue(pinpointAuthentication.isAuthenticated());
    }

    @Test
    public void createPinpointAuthentication2Test() {
        final SecurityServiceImpl securityService = new SecurityServiceImpl();
        final String userId = "test_userId";

        UserService userService = mock(UserService.class);
        when((userService.selectUserByUserId(userId))).thenReturn(null);
        ReflectionTestUtils.setField(securityService, "userService", userService);


        UserGroupService userGroupService = mock(UserGroupService.class);
        when(userGroupService.selectUserGroupByUserId(userId)).thenReturn(null);
        ReflectionTestUtils.setField(securityService, "userGroupService", userGroupService);

        ApplicationConfigService configService = mock(ApplicationConfigService.class);
        when(configService.isManager(userId)).thenReturn(false);
        ReflectionTestUtils.setField(securityService, "configService", configService);

        PinpointAuthentication pinpointAuthentication = securityService.createPinpointAuthentication(userId);

        assertNotNull(pinpointAuthentication);
        assertEquals(pinpointAuthentication.getPrincipal(), "");
        assertEquals(pinpointAuthentication.getName(), "");
        assertFalse(pinpointAuthentication.isAuthenticated());
        assertFalse(pinpointAuthentication.isPinpointManager());
        assertEquals(pinpointAuthentication.getUserGroupList().size(), 0);
    }
}