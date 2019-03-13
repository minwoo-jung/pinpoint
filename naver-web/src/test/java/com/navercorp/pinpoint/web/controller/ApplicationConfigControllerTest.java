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
package com.navercorp.pinpoint.web.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context-naver.xml", "classpath:applicationContext-web-naver.xml"})
public class ApplicationConfigControllerTest {

    private static String TEST_APPLICATION_ID = "testApplication";
    private static String TEST_USER_GROUP_ID1 = "tesUserGroup01";
    private static String TEST_USER_GROUP_USER1 = "KR99999";
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private ApplicationConfigDao appConfigDao;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        
//        AppUserGroupAuth appAuth1 = new AppUserGroupAuth("", TEST_APPLICATION_ID, "tesUserGroup01", Position.MANAGER.toString(), "");
//        appConfigDao.deleteAppAuthUserGroup(appAuth1);
    }
    
    @After
    public void after(){
    }
    
    @Test
    public void insertAndDeleteUserGroup() throws Exception {
//        MvcResult andReturn = this.mockMvc.perform(post("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + TEST_USER_GROUP_USER1 + "\"," + "\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"role\" : \"" + Position.MANAGER.getName() + "\", \"configuration\" :" + "{\"apiMetaData\":true}" + "}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$", hasKey("result")))
//                    .andReturn();

//        MvcResult andReturn = this.mockMvc.perform(put("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + TEST_USER_GROUP_USER1 + "\"," + "\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"role\" : \"" + Position.MANAGER.getName() + "\", \"configuration\" :" + "{\"apiMetaData\":true}" + "}"))
//                .andExpect(status().isOk())
////                    .andExpect(content().contentType("application/json;charset=UTF-8"))
////                    .andExpect(jsonPath("$", hasKey("result")))
//                .andReturn();
        
//        MvcResult andReturn;
//        try {
//            andReturn = this.mockMvc.perform(get("/application/userGroupAuth.pinpoint?applicationId=" + TEST_APPLICATION_ID + "&userId=" + TEST_USER_GROUP_USER1).contentType(MediaType.APPLICATION_JSON))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
////                    .andExpect(jsonPath("$[0]", hasKey("applicationId")))
////                    .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
////                    .andExpect(jsonPath("$[0]", hasKey("authority")))
//                        .andReturn();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
        
//        System.out.println(andReturn.getResponse().getContentAsString());
        
//        this.mockMvc.perform(delete("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\"}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$", hasKey("result")))
//                    .andReturn();
    }
    
    @Test
    public void updateUserGroup() throws Exception {
//        this.mockMvc.perform(post("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"authority\" : \"" + Position.MANAGER.getName() + "\"}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$", hasKey("result")))
//                    .andReturn();
//        
//        this.mockMvc.perform(put("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"authority\" : \"" + Position.USER.getName() + "\"}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$", hasKey("result")))
//                    .andReturn();
//        
//        this.mockMvc.perform(get("/application/userGroupAuth.pinpoint?applicationId=" + TEST_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andExpect(jsonPath("$[0]", hasKey("applicationId")))
//                    .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
//                    .andExpect(jsonPath("$[0]", hasKey("authority")))
//                    .andExpect(jsonPath("$[0].authority").value("user"))
//                    .andReturn();
//        
//        MvcResult andReturn = this.mockMvc.perform(delete("/application/userGroupAuth.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + TEST_USER_GROUP_USER1 + "\"," + "\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\"}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
////                    .andExpect(jsonPath("$", hasKey("result")))
//                    .andReturn();
//        System.out.println(andReturn.getResponse().getContentAsString());
    }

}
