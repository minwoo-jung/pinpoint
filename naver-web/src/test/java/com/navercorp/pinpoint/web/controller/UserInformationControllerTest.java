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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.dao.RoleDao;
import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.service.UserAccountService;
import com.navercorp.pinpoint.web.service.UserInformationService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserAccount;
import com.navercorp.pinpoint.web.vo.UserInformation;
import com.navercorp.pinpoint.web.vo.UserRole;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context-naver.xml", "classpath:applicationContext-web-naver.xml"})
public class UserInformationControllerTest {

    private final static String USER_ID = "naver00";
    private final static String USER_NAME = "minwoo";
    private final static String USER_NAME_UPDATED = "minwoo.jung";
    private final static String USER_DEPARTMENT = "Web platfrom development team";
    private final static String USER_PHONENUMBER = "01012347890";
    private final static String USER_PHONENUMBER_UPDATED = "01000000000";
    private final static String USER_EMAIL = "min@naver.com";
    private final static String USER_EMAIL_UPDATED = "minwoo@naver.com";

    private final static String PASSWORD = "password";
    private final static String PASSWORD_UPDATED = "passwordUpdate";

    private final String ROLE_ID_1 = "roleId1";
    private final String ROLE_ID_2 = "roleId2";
    private final String ROLE_ID_3 = "roleId3";
    private final String ROLE_ID_4 = "roleId4";
    private final String ROLE_ID_5 = "roleId5";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    UserInformationService userInformationService;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserAccountService userAccountService;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void after() {
        userInformationService.deleteUserInformation(USER_ID);
        roleService.deleteUserRole(USER_ID);
        roleService.deleteRoleInformation(ROLE_ID_1);
        roleService.deleteRoleInformation(ROLE_ID_2);
        roleService.deleteRoleInformation(ROLE_ID_3);
        roleService.deleteRoleInformation(ROLE_ID_4);
        roleService.deleteRoleInformation(ROLE_ID_5);
    }

    @Test
    public void selectUsers() throws Exception {
        try {
            User user = new User(USER_ID, USER_NAME, USER_DEPARTMENT, USER_PHONENUMBER, USER_EMAIL);
            UserAccount userAccount = new UserAccount(USER_ID, PASSWORD);
            List<String> roleList = new ArrayList<>();
            roleList.add("admin");
            roleList.add("user");
            UserRole userRole = new UserRole(USER_ID, roleList);
            UserInformation userInformation = new UserInformation(user, userAccount, userRole);
            userInformationService.insertUserInformation(userInformation);

            this.mockMvc.perform(get("/users.pinpoint").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0]", hasKey("userId")))
                .andExpect(jsonPath("$[0]", hasKey("name")))
                .andExpect(jsonPath("$[0]", hasKey("department")))
                .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
                .andExpect(jsonPath("$[0]", hasKey("email")))
                .andReturn();
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }

    private void insertUserInfo() {
        User user = new User(USER_ID, USER_NAME, USER_DEPARTMENT, USER_PHONENUMBER, USER_EMAIL);
        UserAccount userAccount = new UserAccount(USER_ID, PASSWORD);
        List<String> roleList = new ArrayList<>();
        roleList.add("admin");
        roleList.add("user");
        UserRole userRole = new UserRole(USER_ID, roleList);
        UserInformation userInformation = new UserInformation(user, userAccount, userRole);
        userInformationService.insertUserInformation(userInformation);
    }

    @Test
    public void selectUser() throws Exception {
        try {
            insertUserInfo();

            MvcResult mvcResult = this.mockMvc.perform(get("/users/user.pinpoint?userId=" + USER_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.profile.userId").value(USER_ID))
                .andExpect(jsonPath("$.role.roleList").isArray())
                .andReturn();

        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }


    @Test
    public void insertUserInformation() throws Exception {
        try {
            this.mockMvc.perform(post("/users/user.pinpoint")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                        "{" +
                                            "\"profile\" : "
                                                            + "{"
                                                                + "\"userId\" : \"" + USER_ID + "\""
                                                                + ", \"name\" : \"" + USER_NAME + "\""
                                                                + ", \"department\" : \"" + USER_DEPARTMENT + "\""
                                                                + ", \"phoneNumber\" : \"" + USER_PHONENUMBER + "\""
                                                                + ", \"email\" : \"" + USER_EMAIL + "\""
                                                            + "}"
                                            + ",\"account\" : "
                                                            + "{"
                                                                + "\"password\" : \"" + PASSWORD + "\""
                                                            + "}"
                                            + ",\"role\" : "
                                                            + "{"
                                                                + "\"roleList\" : [\"admin\", \"user\"]"
                                                            + "}"
                                        + "}"
                                        )
                                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();

            UserInformation userInformation = userInformationService.selectUserInformation(USER_ID);
            UserAccount userAccount = userAccountService.selectUserAccount(USER_ID);

            assertEquals(userInformation.getProfile().getUserId(), USER_ID);
            assertEquals(userInformation.getRole().getRoleList().size(), 2);
            assertEquals(userAccount.getPassword(), PASSWORD);
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }

    @Test
    public void deleteUserInformation() throws Exception {
        try {
            insertUserInfo();
            assertNotNull(userInformationService.selectUserInformation(USER_ID));

            this.mockMvc.perform(delete("/users/user.pinpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"userId\" : \"minwoo_test\""
                    + "}"
                )
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }

    @Test
    public void updateUserProfile() throws Exception {
        try {
            insertUserInfo();
            assertNotNull(userInformationService.selectUserInformation(USER_ID));

            this.mockMvc.perform(put("/users/user/profile.pinpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{"
                            + "\"userId\" : \"" + USER_ID + "\""
                            + ", \"name\" : \"" + USER_NAME_UPDATED + "\""
                            + ", \"department\" : \"" + USER_DEPARTMENT + "\""
                            + ", \"phoneNumber\" : \"" + USER_PHONENUMBER_UPDATED + "\""
                            + ", \"email\" : \"" + USER_EMAIL_UPDATED + "\""
                        + "}"
                )
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();

            User user = userService.selectUserByUserId(USER_ID);
            assertEquals(user.getName(), USER_NAME_UPDATED);
            assertEquals(user.getPhoneNumber(), USER_PHONENUMBER_UPDATED);
            assertEquals(user.getEmail(), USER_EMAIL_UPDATED);
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }

    @Test
    public void updateUserAccount() throws Exception {
        try {
            insertUserInfo();
            assertNotNull(userInformationService.selectUserInformation(USER_ID));

            this.mockMvc.perform(put("/users/user/account.pinpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"userId\" : \"" + USER_ID + "\""
                        + ", \"currentPassword\" : \"" + PASSWORD + "\""
                        + ", \"newPassword\" : \"" + PASSWORD_UPDATED + "\""
                    + "}"
                )
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();

            UserAccount userAccount = userAccountService.selectUserAccount(USER_ID);
            assertEquals(userAccount.getPassword(), PASSWORD_UPDATED);
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }


    @Test
    public void updateUserRole() throws Exception {
        try {
            insertUserInfo();
            assertNotNull(userInformationService.selectUserInformation(USER_ID));

            this.mockMvc.perform(put("/users/user/role.pinpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"userId\" : \"" + USER_ID + "\""
                        + ",\"roleList\" : [\"admin\", \"user\", \"emp\"]"
                    + "}"
                )
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();

            UserRole userRole = roleService.selectUserRole(USER_ID);
            assertEquals(userRole.getRoleList().size(), 3);
        } finally {
            userInformationService.deleteUserInformation(USER_ID);
        }
    }

    @Test
    public void selectPermissionAndConfiguration() throws Exception {
        try {
            insertRoleInfo();

            MvcResult mvcResult = this.mockMvc.perform(get("/users/user/permissionAndConfiguration.pinpoint").header("SSO_USER", USER_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.configuration ").isMap())
                .andExpect(jsonPath("$.permission").isMap())
                .andReturn();

            logger.info("result : " + mvcResult.getResponse().getContentAsString());

        } finally {
            roleService.deleteUserRole(USER_ID);
            roleService.deleteRoleInformation(ROLE_ID_1);
            roleService.deleteRoleInformation(ROLE_ID_2);
            roleService.deleteRoleInformation(ROLE_ID_3);
            roleService.deleteRoleInformation(ROLE_ID_4);
            roleService.deleteRoleInformation(ROLE_ID_5);
        }
    }

    private void insertRoleInfo() {
        PermsGroupAdministration permsGroupAdministration1 = new PermsGroupAdministration(false, false, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization1 = new PermsGroupAppAuthorization(false, false, false);
        PermsGroupAlarm permsGroupAlarm1 = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup1 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection1 = new PermissionCollection(permsGroupAdministration1, permsGroupAppAuthorization1, permsGroupAlarm1, permsGroupUserGroup1);
        RoleInformation roleInformation1 = new RoleInformation(ROLE_ID_1, permissionCollection1);
        roleService.insertRoleInformation(roleInformation1);

        PermsGroupAdministration permsGroupAdministration2 = new PermsGroupAdministration(true, false, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization2 = new PermsGroupAppAuthorization(true, false, false);
        PermsGroupAlarm permsGroupAlarm2 = new PermsGroupAlarm(true, false);
        PermsGroupUserGroup permsGroupUserGroup2 = new PermsGroupUserGroup(true, false);
        PermissionCollection permissionCollection2 = new PermissionCollection(permsGroupAdministration2, permsGroupAppAuthorization2, permsGroupAlarm2, permsGroupUserGroup2);
        RoleInformation roleInformation2 = new RoleInformation(ROLE_ID_2, permissionCollection2);
        roleService.insertRoleInformation(roleInformation2);

        PermsGroupAdministration permsGroupAdministration3 = new PermsGroupAdministration(false, true, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization3 = new PermsGroupAppAuthorization(false, true, false);
        PermsGroupAlarm permsGroupAlarm3 = new PermsGroupAlarm(false, true);
        PermsGroupUserGroup permsGroupUserGroup3 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection3 = new PermissionCollection(permsGroupAdministration3, permsGroupAppAuthorization3, permsGroupAlarm3, permsGroupUserGroup3);
        RoleInformation roleInformation3 = new RoleInformation(ROLE_ID_3, permissionCollection3);
        roleService.insertRoleInformation(roleInformation3);

        PermsGroupAdministration permsGroupAdministration4 = new PermsGroupAdministration(false, true, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization4 = new PermsGroupAppAuthorization(false, true, false);
        PermsGroupAlarm permsGroupAlarm4 = new PermsGroupAlarm(false, true);
        PermsGroupUserGroup permsGroupUserGroup4 = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection4 = new PermissionCollection(permsGroupAdministration4, permsGroupAppAuthorization4, permsGroupAlarm4, permsGroupUserGroup4);
        RoleInformation roleInformation4 = new RoleInformation(ROLE_ID_4, permissionCollection4);
        roleService.insertRoleInformation(roleInformation4);

        PermsGroupAdministration permsGroupAdministration5 = new PermsGroupAdministration(false, false, true);
        PermsGroupAppAuthorization permsGroupAppAuthorization5 = new PermsGroupAppAuthorization(false, false, false);
        PermsGroupAlarm permsGroupAlarm5 = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup5 = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection5 = new PermissionCollection(permsGroupAdministration5, permsGroupAppAuthorization5, permsGroupAlarm5, permsGroupUserGroup5);
        RoleInformation roleInformation5 = new RoleInformation(ROLE_ID_5, permissionCollection5);
        roleService.insertRoleInformation(roleInformation5);

        List<String> roleList = new ArrayList<String>(5);
        roleList.add(ROLE_ID_1);
        roleList.add(ROLE_ID_2);
        roleList.add(ROLE_ID_3);
        roleList.add(ROLE_ID_4);
        roleList.add(ROLE_ID_5);
        UserRole userRole = new UserRole(USER_ID, roleList);
        roleService.insertUserRole(userRole);
    }

}