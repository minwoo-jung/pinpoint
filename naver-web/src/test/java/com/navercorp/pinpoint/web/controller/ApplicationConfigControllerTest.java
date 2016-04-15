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

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
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

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.ApplicationAuthority;
import com.navercorp.pinpoint.web.vo.ApplicationAuthority.AuthorityLevel;

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
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private ApplicationConfigDao appConfigDao;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        
        ApplicationAuthority appAuth1 = new ApplicationAuthority("", TEST_APPLICATION_ID, "tesUserGroup01", AuthorityLevel.MANAGER.getName());
        appConfigDao.deleteAuthority(appAuth1);
    }
    
    @After
    public void after(){
    }
    
    @Test
    public void insertAndDeleteUserGroup() throws Exception {
        this.mockMvc.perform(post("/application/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"authority\" : \"" + AuthorityLevel.MANAGER.getName() + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$", hasKey("result")))
                    .andReturn();
        
        this.mockMvc.perform(get("/application/userGroup.pinpoint?applicationId=" + TEST_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$[0]", hasKey("applicationId")))
                    .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
                    .andExpect(jsonPath("$[0]", hasKey("authority")))
                    .andReturn();
        
        this.mockMvc.perform(delete("/application/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$", hasKey("result")))
                    .andReturn();
    }
    
    @Test
    public void updateUserGroup() throws Exception {
        this.mockMvc.perform(post("/application/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"authority\" : \"" + AuthorityLevel.MANAGER.getName() + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$", hasKey("result")))
                    .andReturn();
        
        this.mockMvc.perform(put("/application/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\", \"authority\" : \"" + AuthorityLevel.USER.getName() + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$", hasKey("result")))
                    .andReturn();
        
        this.mockMvc.perform(get("/application/userGroup.pinpoint?applicationId=" + TEST_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$[0]", hasKey("applicationId")))
                    .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
                    .andExpect(jsonPath("$[0]", hasKey("authority")))
                    .andExpect(jsonPath("$[0].authority").value("user"))
                    .andReturn();
        
        this.mockMvc.perform(delete("/application/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\" : \"" + TEST_APPLICATION_ID + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID1 + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$", hasKey("result")))
                    .andReturn();
        
    }

}
