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
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class AppConfigOrganizerTest {

    @Test
    public void getApplicationConfigurationTest() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();
        ApplicationConfigService applicationConfigService = mock(ApplicationConfigService.class);

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);

        when(applicationConfigService.selectApplicationConfiguration(applicationId)).thenReturn(applicationConfiguration);
        ReflectionTestUtils.setField(appConfigOrganizer, "applicationConfigService", applicationConfigService);
        ApplicationConfiguration applicationConfig = appConfigOrganizer.getApplicationConfiguration(new PinpointAuthentication(), applicationId);
        assertEquals(applicationConfig, applicationConfiguration);
    }

    @Test
    public void getApplicationConfiguration2Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication();
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        ApplicationConfiguration applicationConfig = appConfigOrganizer.getApplicationConfiguration(pinpointAuthentication, applicationId);
        assertEquals(applicationConfig, applicationConfiguration);
    }

    @Test
    public void userGroupAuthTest() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        final String userGroupId2 = "userGroupId2";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, userGroupId2, AppUserGroupAuth.Position.USER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth3 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.GUEST.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        appUserGroupAuthList.add(appUserGroupAuth3);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication();
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        List<AppUserGroupAuth> appUserGroupAuths = appConfigOrganizer.userGroupAuth(pinpointAuthentication, applicationId);
        assertEquals(appUserGroupAuths.size(), 1);
        AppUserGroupAuth appUserGroupAuth = appUserGroupAuths.get(0);
        assertEquals(appUserGroupAuth.getApplicationId(), applicationId);
        assertEquals(appUserGroupAuth.getUserGroupId(), "guest");
    }

    @Test
    public void userGroupAuth2Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        final String userGroupId2 = "userGroupId2";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, userGroupId2, AppUserGroupAuth.Position.USER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth3 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.GUEST.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        appUserGroupAuthList.add(appUserGroupAuth3);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);

        List<UserGroup> userGroupList = new ArrayList<>(1);
        UserGroup userGroup = new UserGroup("1", userGroupId);
        userGroupList.add(userGroup);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication("userId", "name", userGroupList, true, false, RoleInformation.UNASSIGNED_ROLE);
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        List<AppUserGroupAuth> appUserGroupAuths = appConfigOrganizer.userGroupAuth(pinpointAuthentication, applicationId);
        assertEquals(appUserGroupAuths.size(), 1);
        AppUserGroupAuth appUserGroupAuth = appUserGroupAuths.get(0);
        assertEquals(appUserGroupAuth.getApplicationId(), applicationId);
        assertEquals(appUserGroupAuth.getUserGroupId(), userGroupId);
        assertEquals(appUserGroupAuth.getPosition(), AppUserGroupAuth.Position.MANAGER);
    }

    @Test
    public void isEmptyUserGroupTest() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        final String userGroupId2 = "userGroupId2";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth3 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.GUEST.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth3);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication();
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        boolean isEmptyUserGroup = appConfigOrganizer.isEmptyUserGroup(pinpointAuthentication, applicationId);
        assertFalse(isEmptyUserGroup);
    }

    @Test
    public void isEmptyUserGroup2Test() {
        final String applicationId = "applicationId";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication();
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        boolean isEmptyUserGroup = appConfigOrganizer.isEmptyUserGroup(pinpointAuthentication, applicationId);
        assertTrue(isEmptyUserGroup);
    }

    @Test
    public void isPinpointManagerTest() {
        final String applicationId = "applicationId";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication();
        pinpointAuthentication.addApplicationConfiguration(applicationConfiguration);

        boolean isPinpointManager = appConfigOrganizer.isPinpointManager(pinpointAuthentication);
        assertFalse(isPinpointManager);
    }

    @Test
    public void isPinpointManager2Test() {
        final String applicationId = "applicationId";
        AppConfigOrganizer appConfigOrganizer = new AppConfigOrganizer();

        List<UserGroup> userGroupList = new ArrayList<>(0);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication("userId", "name", userGroupList, true, true, RoleInformation.UNASSIGNED_ROLE);

        boolean isPinpointManager = appConfigOrganizer.isPinpointManager(pinpointAuthentication);
        assertTrue(isPinpointManager);
    }

}