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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.RoleDao;
import com.navercorp.pinpoint.web.vo.UserRole;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class RoleServiceImplTest {

    private final String USER_ID = "testUser";
    private final String ROLE_ID_1 = "roleId1";
    private final String ROLE_ID_2 = "roleId2";
    private final String ROLE_ID_3 = "roleId3";
    private final String ROLE_ID_4 = "roleId4";
    private final String ROLE_ID_5 = "roleId5";

    @Test
    public void mergeRoleInformation1() {
        RoleDao roleDao = mock(RoleDao.class);

        List<String> roleList = new ArrayList<String>(5);
        roleList.add(ROLE_ID_1);
        roleList.add(ROLE_ID_2);
        roleList.add(ROLE_ID_3);
        roleList.add(ROLE_ID_4);
        roleList.add(ROLE_ID_5);
        UserRole userRole = new UserRole(USER_ID, roleList);
        when(roleDao.selectUserRole(USER_ID)).thenReturn(userRole);

        PermsGroupAdministration permsGroupAdministration1 = new PermsGroupAdministration(false, false, false, true);
        PermsGroupAppAuthorization permsGroupAppAuthorization1 = new PermsGroupAppAuthorization(false, false, false, false);
        PermsGroupAlarm permsGroupAlarm1 = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup1 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection1 = new PermissionCollection(permsGroupAdministration1, permsGroupAppAuthorization1, permsGroupAlarm1, permsGroupUserGroup1);
        RoleInformation roleInformation1 = new RoleInformation(ROLE_ID_1, permissionCollection1);
        when(roleDao.selectRoleInformation(ROLE_ID_1)).thenReturn(roleInformation1);

        PermsGroupAdministration permsGroupAdministration2 = new PermsGroupAdministration(true, false, false, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization2 = new PermsGroupAppAuthorization(true, false, false, false);
        PermsGroupAlarm permsGroupAlarm2 = new PermsGroupAlarm(true, false);
        PermsGroupUserGroup permsGroupUserGroup2 = new PermsGroupUserGroup(true, false);
        PermissionCollection permissionCollection2 = new PermissionCollection(permsGroupAdministration2, permsGroupAppAuthorization2, permsGroupAlarm2, permsGroupUserGroup2);
        RoleInformation roleInformation2 = new RoleInformation(ROLE_ID_2, permissionCollection2);
        when(roleDao.selectRoleInformation(ROLE_ID_2)).thenReturn(roleInformation2);

        PermsGroupAdministration permsGroupAdministration3 = new PermsGroupAdministration(false, true, false, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization3 = new PermsGroupAppAuthorization(false, true, false, false);
        PermsGroupAlarm permsGroupAlarm3 = new PermsGroupAlarm(false, true);
        PermsGroupUserGroup permsGroupUserGroup3 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection3 = new PermissionCollection(permsGroupAdministration3, permsGroupAppAuthorization3, permsGroupAlarm3, permsGroupUserGroup3);
        RoleInformation roleInformation3 = new RoleInformation(ROLE_ID_3, permissionCollection3);
        when(roleDao.selectRoleInformation(ROLE_ID_3)).thenReturn(roleInformation3);

        PermsGroupAdministration permsGroupAdministration4 = new PermsGroupAdministration(false, true, false, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization4 = new PermsGroupAppAuthorization(false, true, false, false);
        PermsGroupAlarm permsGroupAlarm4 = new PermsGroupAlarm(false, true);
        PermsGroupUserGroup permsGroupUserGroup4 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection4 = new PermissionCollection(permsGroupAdministration4, permsGroupAppAuthorization4, permsGroupAlarm4, permsGroupUserGroup4);
        RoleInformation roleInformation4 = new RoleInformation(ROLE_ID_4, permissionCollection4);
        when(roleDao.selectRoleInformation(ROLE_ID_4)).thenReturn(roleInformation4);

        PermsGroupAdministration permsGroupAdministration5 = new PermsGroupAdministration(false, false, true, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization5 = new PermsGroupAppAuthorization(false, false, false ,true);
        PermsGroupAlarm permsGroupAlarm5 = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup5 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection5 = new PermissionCollection(permsGroupAdministration5, permsGroupAppAuthorization5, permsGroupAlarm5, permsGroupUserGroup5);
        RoleInformation roleInformation5 = new RoleInformation(ROLE_ID_5, permissionCollection5);
        when(roleDao.selectRoleInformation(ROLE_ID_5)).thenReturn(roleInformation5);

        RoleService roleService = new RoleServiceImpl();
        ReflectionTestUtils.setField(roleService, "roleDao", roleDao);
        RoleInformation userPermission = roleService.getUserPermission(USER_ID);

        PermissionCollection permissionCollection = userPermission.getPermissionCollection();

        PermsGroupAdministration permsGroupAdministration = permissionCollection.getPermsGroupAdministration();
        assertTrue(permsGroupAdministration.getViewAdminMenu());
        assertTrue(permsGroupAdministration.getEditUser());
        assertTrue(permsGroupAdministration.getEditRole());
        assertTrue(permsGroupAdministration.getCallAdminApi());

        PermsGroupAppAuthorization permsGroupAppAuthorization = permissionCollection.getPermsGroupAppAuthorization();
        assertTrue(permsGroupAppAuthorization.getPreoccupancy());
        assertTrue(permsGroupAppAuthorization.getEditAuthorForEverything());
        assertFalse(permsGroupAppAuthorization.getEditAuthorOnlyManager());
        assertTrue(permsGroupAppAuthorization.getObtainAllAuthorization());

        PermsGroupAlarm permsGroupAlarm = permissionCollection.getPermsGroupAlarm();
        assertTrue(permsGroupAlarm.getEditAlarmForEverything());
        assertTrue(permsGroupAlarm.getEditAlarmOnlyManager());

        PermsGroupUserGroup permsGroupUserGroup = permissionCollection.getPermsGroupUserGroup();
        assertTrue(permsGroupUserGroup.getEditGroupForEverything());
        assertTrue(permsGroupUserGroup.getEditGroupOnlyGroupMember());
    }

    @Test
    public void mergeRoleInformation2() {
        RoleDao roleDao = mock(RoleDao.class);

        List<String> roleList = new ArrayList<String>(0);
        UserRole userRole = new UserRole(USER_ID, roleList);
        when(roleDao.selectUserRole(USER_ID)).thenReturn(userRole);

        RoleService roleService = new RoleServiceImpl();
        ReflectionTestUtils.setField(roleService, "roleDao", roleDao);
        assertEquals(roleService.getUserPermission(USER_ID), RoleInformation.UNASSIGNED_ROLE);
    }

    @Test
    public void mergeRoleInformation3() {
        RoleDao roleDao = mock(RoleDao.class);

        List<String> roleList = new ArrayList<String>(5);
        roleList.add(ROLE_ID_1);
        roleList.add(ROLE_ID_2);
        roleList.add(ROLE_ID_3);
        roleList.add(ROLE_ID_4);
        roleList.add(ROLE_ID_5);
        UserRole userRole = new UserRole(USER_ID, roleList);
        when(roleDao.selectUserRole(USER_ID)).thenReturn(userRole);

        when(roleDao.selectRoleInformation(ROLE_ID_1)).thenReturn(null);
        when(roleDao.selectRoleInformation(ROLE_ID_2)).thenReturn(null);
        when(roleDao.selectRoleInformation(ROLE_ID_3)).thenReturn(null);
        when(roleDao.selectRoleInformation(ROLE_ID_4)).thenReturn(null);
        when(roleDao.selectRoleInformation(ROLE_ID_5)).thenReturn(null);

        RoleService roleService = new RoleServiceImpl();
        ReflectionTestUtils.setField(roleService, "roleDao", roleDao);
        assertEquals(roleService.getUserPermission(USER_ID), RoleInformation.UNASSIGNED_ROLE);
    }

}