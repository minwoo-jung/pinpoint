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

package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.role.*;
import org.apache.hadoop.hbase.protobuf.generated.AccessControlProtos;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class PermissionCheckerTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private PermissionChecker permissionChecker = new PermissionChecker();

    @Test
    public void baseTest() {
        PinpointAuthentication authentication = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, RoleInformation.UNASSIGNED_ROLE);
        boolean result = permissionChecker.checkPermission(authentication, "", "");
        assertFalse(result);
    }

    @Test
    public void PermsGroupAdministrationTest01() {
        PermsGroupAdministration permsGroupAdministration01 = new PermsGroupAdministration(true, false, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(permsGroupAdministration01, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_administration_viewAdminMenu", "");
        assertTrue(result01);

        PermsGroupAdministration permsGroupAdministration02 = new PermsGroupAdministration(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(permsGroupAdministration02, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_administration_viewAdminMenu","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupAdministrationTest02() {
        PermsGroupAdministration permsGroupAdministration01 = new PermsGroupAdministration(false, true, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(permsGroupAdministration01, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_administration_editUser","");
        assertTrue(result01);

        PermsGroupAdministration permsGroupAdministration02 = new PermsGroupAdministration(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(permsGroupAdministration02, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_administration_editUser","");
        assertFalse(result02);

        PermsGroupAdministration permsGroupAdministration03 = new PermsGroupAdministration(false, false, false);
        PermissionCollection permissionCollection03 = new PermissionCollection(permsGroupAdministration03, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation03 = new RoleInformation("testRole", permissionCollection03);
        PinpointAuthentication authentication03 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation03);
        boolean result03 = permissionChecker.checkPermission(authentication03, "permission_administration_editUser","test_id");
        assertTrue(result03);

        PermsGroupAdministration permsGroupAdministration04 = new PermsGroupAdministration(false, false, false);
        PermissionCollection permissionCollection04 = new PermissionCollection(permsGroupAdministration04, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation04 = new RoleInformation("testRole", permissionCollection04);
        PinpointAuthentication authentication04 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation04);
        boolean result04 = permissionChecker.checkPermission(authentication04, "permission_administration_editUser",new Object());
        assertFalse(result04);
    }

    @Test
    public void PermsGroupAdministrationTest03() {
        PermsGroupAdministration permsGroupAdministration01 = new PermsGroupAdministration(false, true, true);
        PermissionCollection permissionCollection01 = new PermissionCollection(permsGroupAdministration01, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_administration_editRole","");
        assertTrue(result01);

        PermsGroupAdministration permsGroupAdministration02 = new PermsGroupAdministration(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(permsGroupAdministration02, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_administration_editRole","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupAppAuthorizationTest01() {
        PermsGroupAppAuthorization permsGroupAppAuthorization01 = new PermsGroupAppAuthorization(true, false, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization01, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_appAuthorization_preoccupancy","");
        assertTrue(result01);

        PermsGroupAppAuthorization permsGroupAppAuthorization02 = new PermsGroupAppAuthorization(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization02, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_appAuthorization_preoccupancy","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupAppAuthorizationTest02() {
        PermsGroupAppAuthorization permsGroupAppAuthorization01 = new PermsGroupAppAuthorization(false, true, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization01, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_appAuthorization_editAuthorForEverything","");
        assertTrue(result01);

        PermsGroupAppAuthorization permsGroupAppAuthorization02 = new PermsGroupAppAuthorization(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization02, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_appAuthorization_editAuthorForEverything","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupAppAuthorizationTest03() {
        PermsGroupAppAuthorization permsGroupAppAuthorization01 = new PermsGroupAppAuthorization(false, true, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization01, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_appAuthorization_editAuthorOnlyManager","");
        assertTrue(result01);

        PermsGroupAppAuthorization permsGroupAppAuthorization02 = new PermsGroupAppAuthorization(false, false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization02, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_appAuthorization_editAuthorOnlyManager","");
        assertFalse(result02);

        final String applicationId ="testApplication";
        final String userId = "testId";
        ApplicationConfigService applicationConfigService01 = mock(ApplicationConfigService.class);
        when(applicationConfigService01.canEditConfiguration(applicationId, userId)).thenReturn(true);
        ReflectionTestUtils.setField(permissionChecker, "applicationConfigService", applicationConfigService01);
        PermsGroupAppAuthorization permsGroupAppAuthorization03 = new PermsGroupAppAuthorization(false, false, true);
        PermissionCollection permissionCollection03 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization03, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation03 = new RoleInformation("testRole", permissionCollection03);
        PinpointAuthentication authentication03 = new PinpointAuthentication(userId, "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation03);
        boolean result03 = permissionChecker.checkPermission(authentication03, "permission_appAuthorization_editAuthorOnlyManager",applicationId);
        assertTrue(result03);

        ApplicationConfigService applicationConfigService02 = mock(ApplicationConfigService.class);
        when(applicationConfigService02.canEditConfiguration(applicationId, userId)).thenReturn(false);
        ReflectionTestUtils.setField(permissionChecker, "applicationConfigService", applicationConfigService02);
        PermsGroupAppAuthorization permsGroupAppAuthorization04 = new PermsGroupAppAuthorization(false, false, true);
        PermissionCollection permissionCollection04 = new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization04, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation04 = new RoleInformation("testRole", permissionCollection04);
        PinpointAuthentication authentication04 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation04);
        boolean result04 = permissionChecker.checkPermission(authentication04, "permission_appAuthorization_editAuthorOnlyManager",applicationId);
        assertFalse(result04);
    }

    @Test
    public void PermsGroupAlarmTest01(){
        PermsGroupAlarm permsGroupAlarm01 = new PermsGroupAlarm(true, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm01, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_alarm_editAlarmForEverything","");
        assertTrue(result01);

        PermsGroupAlarm permsGroupAlarm02 = new PermsGroupAlarm(false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm02, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_alarm_editAlarmForEverything","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupAlarmTest02(){
        PermsGroupAlarm permsGroupAlarm03 = new PermsGroupAlarm(true, false);
        PermissionCollection permissionCollection03 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm03, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation03 = new RoleInformation("testRole", permissionCollection03);
        PinpointAuthentication authentication03 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation03);
        boolean result03 = permissionChecker.checkPermission(authentication03, "permission_alarm_editAlarmOnlyGroupMember","");
        assertTrue(result03);

        final String applicationId ="testApplication";
        final String userId = "testId";
        ApplicationConfigService applicationConfigService01 = mock(ApplicationConfigService.class);
        when(applicationConfigService01.canEditConfiguration(applicationId, userId)).thenReturn(true);
        ReflectionTestUtils.setField(permissionChecker, "applicationConfigService", applicationConfigService01);
        PermsGroupAlarm permsGroupAlarm01 = new PermsGroupAlarm(false, true);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm01, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication(userId, "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_alarm_editAlarmOnlyGroupMember",applicationId);
        assertTrue(result01);

        ApplicationConfigService applicationConfigService02 = mock(ApplicationConfigService.class);
        when(applicationConfigService02.canEditConfiguration(applicationId, userId)).thenReturn(false);
        ReflectionTestUtils.setField(permissionChecker, "applicationConfigService", applicationConfigService02);
        PermsGroupAlarm permsGroupAlarm04 = new PermsGroupAlarm(false, true);
        PermissionCollection permissionCollection04 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm04, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation04 = new RoleInformation("testRole", permissionCollection04);
        PinpointAuthentication authentication04 = new PinpointAuthentication(userId, "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation04);
        boolean result04 = permissionChecker.checkPermission(authentication04, "permission_alarm_editAlarmOnlyGroupMember",applicationId);
        assertFalse(result04);

        PermsGroupAlarm permsGroupAlarm02 = new PermsGroupAlarm(false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, permsGroupAlarm02, PermsGroupUserGroup.DEFAULT);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_alarm_editAlarmOnlyGroupMember","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupUserGroupTest01(){
        PermsGroupUserGroup permsGroupUserGroup01 = new PermsGroupUserGroup(true, false);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup01);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_userGroup_editGroupForEverything","");
        assertTrue(result01);

        PermsGroupUserGroup permsGroupUserGroup02 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup02);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_userGroup_editGroupForEverything","");
        assertFalse(result02);
    }

    @Test
    public void PermsGroupUserGroupTest02(){
        PermsGroupUserGroup permsGroupUserGroup03 = new PermsGroupUserGroup(true, false);
        PermissionCollection permissionCollection03 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup03);
        RoleInformation roleInformation03 = new RoleInformation("testRole", permissionCollection03);
        PinpointAuthentication authentication03 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation03);
        boolean result03 = permissionChecker.checkPermission(authentication03, "permission_userGroup_editGroupOnlyGroupMember","");
        assertTrue(result03);

        final String applicationId ="testApplication";
        final String userId = "testId";
        UserGroupService userGroupService01 = mock(UserGroupService.class);
        when(userGroupService01.checkValid(userId, applicationId)).thenReturn(false);
        ReflectionTestUtils.setField(permissionChecker, "userGroupService", userGroupService01);
        PermsGroupUserGroup permsGroupUserGroup01 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection01 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup01);
        RoleInformation roleInformation01 = new RoleInformation("testRole", permissionCollection01);
        PinpointAuthentication authentication01 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation01);
        boolean result01 = permissionChecker.checkPermission(authentication01, "permission_userGroup_editGroupOnlyGroupMember",userId);
        assertFalse(result01);

        UserGroupService userGroupService02 = mock(UserGroupService.class);
        when(userGroupService02.checkValid(userId, applicationId)).thenReturn(true);
        ReflectionTestUtils.setField(permissionChecker, "userGroupService", userGroupService02);
        PermsGroupUserGroup permsGroupUserGroup02 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection02 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup02);
        RoleInformation roleInformation02 = new RoleInformation("testRole", permissionCollection02);
        PinpointAuthentication authentication02 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation02);
        boolean result02 = permissionChecker.checkPermission(authentication02, "permission_userGroup_editGroupOnlyGroupMember",userId);
        assertFalse(result02);

        PermsGroupUserGroup permsGroupUserGroup04 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection04 = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, permsGroupUserGroup04);
        RoleInformation roleInformation04 = new RoleInformation("testRole", permissionCollection04);
        PinpointAuthentication authentication04 = new PinpointAuthentication("test_id", "test_user", new ArrayList<UserGroup>(0), false, false, roleInformation04);
        boolean result04 = permissionChecker.checkPermission(authentication04, "permission_userGroup_editGroupOnlyGroupMember",userId);
        assertFalse(result04);
    }
}