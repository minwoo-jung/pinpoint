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

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context-naver.xml", "classpath:applicationContext-web-naver.xml"})
public class RoleControllerTest {

    private static String ROLE_ID = "testRole";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    RoleService roleService;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        roleService.deleteRoleInformation(ROLE_ID);
    }

    @Test
    public void insertRole() throws Exception {
        try {
            this.mockMvc.perform(post("/roles/role.pinpoint").contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                            "{\"roleId\" : \"" + ROLE_ID
                                                                + "\"," + "\"permissionCollection\" :"
                                                                                + "{" +
                                                                                    "\"permsGroupAministration\" :"
                                                                                        + "{"
                                                                                            + "\"viewAdminMenu\" : true"
                                                                                            + ", \"editUser\" : true"
                                                                                            + ", \"editRole\" : true"
                                                                                        + "}"
                                                                                    + ",\"permsGroupAppAuthorization\" : "
                                                                                        + "{"
                                                                                            + "\"preoccupancy\" : true"
                                                                                            + ", \"editAuthorForEverything\" : true"
                                                                                            + ", \"editAuthorOnlyManager\" : true"
                                                                                        + "}"
                                                                                    + ",\"permsGroupAlarm\" : "
                                                                                        + "{"
                                                                                            + "\"editAlarmForEverything\" : true"
                                                                                            + ", \"editAlarmOnlyGroupMember\" : true"
                                                                                    + "}"
                                                                                    + ",\"permsGroupUserGroup\" : "
                                                                                    + "{"
                                                                                    + "\"editGroupForEverything\" : true"
                                                                                    + ", \"editGroupOnlyGroupMember\" : true"
                                                                                    + "}"
                                                                                + "}"
                                                            + "}"
                                                                )
                                                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andReturn();

            RoleInformation roleInformation = roleService.selectRoleInformation(ROLE_ID);
            assertEquals(roleInformation.getRoleId(), ROLE_ID);
        } finally {
            roleService.deleteRoleInformation(ROLE_ID);
        }
    }

    private void insertRoleInformation() {
        PermsGroupAdministration permsGroupAdministration = new PermsGroupAdministration(true, true, true, true);
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(true, true, true);
        PermsGroupAlarm permsGroupAlarm = new PermsGroupAlarm(true, true);
        PermsGroupUserGroup permsGroupUserGroup = new PermsGroupUserGroup(true, true);
        PermissionCollection permissionCollection = new PermissionCollection(permsGroupAdministration, permsGroupAppAuthorization, permsGroupAlarm, permsGroupUserGroup);
        RoleInformation roleInformation = new RoleInformation(ROLE_ID, permissionCollection);
        roleService.insertRoleInformation(roleInformation);
    }
    @Test
    public void selectRole() throws Exception {
        try {
            insertRoleInformation();

            this.mockMvc.perform(get("/roles/role.pinpoint?roleId=testRole")
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                        )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.roleId").value(ROLE_ID))
                .andExpect(jsonPath("$.permissionCollection.permsGroupAministration.viewAdminMenu").value(true))
                .andReturn();

        } finally {
            roleService.deleteRoleInformation(ROLE_ID);
        }
    }

    @Test
    public void deleteRole() throws Exception {
        try {
            insertRoleInformation();
            assertEquals(roleService.selectRoleInformation(ROLE_ID).getRoleId(), ROLE_ID);

            this.mockMvc.perform(delete("/roles/role.pinpoint?roleId=" + ROLE_ID).contentType(MediaType.APPLICATION_JSON)
                                )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andReturn();

            assertNull(roleService.selectRoleInformation(ROLE_ID));
        } finally {
            roleService.deleteRoleInformation(ROLE_ID);
        }
    }

    @Test
    public void updateRole() throws Exception {
        try {
            insertRoleInformation();
            assertEquals(roleService.selectRoleInformation(ROLE_ID).getRoleId(), ROLE_ID);

            this.mockMvc.perform(put("/roles/role.pinpoint").contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"roleId\" : \"" + ROLE_ID
                        + "\"," + "\"permissionCollection\" :"
                                        + "{" +
                                            "\"permsGroupAministration\" :"
                                                    + "{"
                                                            + "\"viewAdminMenu\" : true"
                                                            + ", \"editUser\" : false"
                                                            + ", \"editRole\" : true"
                                                    + "}"
                                            + ",\"permsGroupAppAuthorization\" : "
                                                    + "{"
                                                            + "\"preoccupancy\" : false"
                                                            + ", \"editAuthorForEverything\" : true"
                                                            + ", \"editAuthorOnlyManager\" : false"
                                                    + "}"
                                            + ",\"permsGroupAlarm\" : "
                                                    + "{"
                                                            + "\"editAlarmForEverything\" : true"
                                                            + ", \"editAlarmOnlyGroupMember\" : false"
                                                    + "}"
                                            + ",\"permsGroupUserGroup\" : "
                                                    + "{"
                                                            + "\"editGroupForEverything\" : true"
                                                            + ", \"editGroupOnlyGroupMember\" : true"
                                                    + "}"
                                        + "}"
                        + "}"
                )
            )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andReturn();

            assertFalse(roleService.selectRoleInformation(ROLE_ID).getPermissionCollection().getPermsGroupAdministration().getEditUser());
        } finally {
            roleService.deleteRoleInformation(ROLE_ID);
        }
    }
}
