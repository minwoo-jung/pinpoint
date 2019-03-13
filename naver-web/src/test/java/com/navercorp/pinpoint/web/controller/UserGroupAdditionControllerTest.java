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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
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

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context-naver.xml", "classpath:applicationContext-web-naver.xml"})
public class UserGroupAdditionControllerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String TEST_APPLICATION_ID_01 = "testApplication01";
    private static String TEST_APPLICATION_ID_02 = "testApplication02";
    private static String TEST_APPLICATION_ID_03 = "testApplication03";
    private static String TEST_USER_GROUP = "testUserGroup";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ApplicationConfigDao appConfigDao;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void selectAppUserGroupAuthList() throws Exception {
        try {
            insertAppUserGroupAuth();
            MvcResult mvcResult = this.mockMvc.perform(get("/userGroup/applicationAuth.pinpoint?userGroupId=" + TEST_USER_GROUP).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$[0]", hasKey("applicationId")))
//                    .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
//                    .andExpect(jsonPath("$[0]", hasKey("authority")))
                .andReturn();

            logger.info("result :" + mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            deleteAppUserGroupAuth();
        }
    }

    private void insertAppUserGroupAuth() {
        AppAuthConfiguration AppAuthConfiguration01 = new AppAuthConfiguration();
        AppAuthConfiguration01.setServerMapData(true);
        AppAuthConfiguration01.setParamMetaData(true);
        AppUserGroupAuth appAuth01 = new AppUserGroupAuth(TEST_APPLICATION_ID_01, TEST_USER_GROUP, AppUserGroupAuth.Position.MANAGER.toString(), AppAuthConfiguration01);
        appConfigDao.insertAppUserGroupAuth(appAuth01);

        AppAuthConfiguration AppAuthConfiguration02 = new AppAuthConfiguration();
        AppAuthConfiguration02.setApiMetaData(true);
        AppAuthConfiguration02.setSqlMetaData(true);
        AppUserGroupAuth appAuth02 = new AppUserGroupAuth(TEST_APPLICATION_ID_02, TEST_USER_GROUP, AppUserGroupAuth.Position.USER.toString(), AppAuthConfiguration02);
        appConfigDao.insertAppUserGroupAuth(appAuth02);

        AppAuthConfiguration AppAuthConfiguration03 = new AppAuthConfiguration();
        AppUserGroupAuth appAuth03 = new AppUserGroupAuth(TEST_APPLICATION_ID_03, TEST_USER_GROUP, AppUserGroupAuth.Position.MANAGER.toString(), AppAuthConfiguration03);
        appConfigDao.insertAppUserGroupAuth(appAuth03);
    }

    private void deleteAppUserGroupAuth() {
        AppAuthConfiguration AppAuthConfiguration01 = new AppAuthConfiguration();
        AppAuthConfiguration01.setServerMapData(true);
        AppAuthConfiguration01.setParamMetaData(true);
        AppUserGroupAuth appAuth01 = new AppUserGroupAuth(TEST_APPLICATION_ID_01, TEST_USER_GROUP, AppUserGroupAuth.Position.MANAGER.toString(), AppAuthConfiguration01);
        appConfigDao.deleteAppUserGroupAuth(appAuth01);

        AppAuthConfiguration AppAuthConfiguration02 = new AppAuthConfiguration();
        AppAuthConfiguration02.setApiMetaData(true);
        AppAuthConfiguration02.setSqlMetaData(true);
        AppUserGroupAuth appAuth02 = new AppUserGroupAuth(TEST_APPLICATION_ID_02, TEST_USER_GROUP, AppUserGroupAuth.Position.USER.toString(), AppAuthConfiguration02);
        appConfigDao.deleteAppUserGroupAuth(appAuth02);

        AppAuthConfiguration AppAuthConfiguration03 = new AppAuthConfiguration();
        AppUserGroupAuth appAuth03 = new AppUserGroupAuth(TEST_APPLICATION_ID_03, TEST_USER_GROUP, AppUserGroupAuth.Position.MANAGER.toString(), AppAuthConfiguration03);
        appConfigDao.deleteAppUserGroupAuth(appAuth03);
    }

}