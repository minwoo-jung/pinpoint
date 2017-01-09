/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
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
import org.springframework.test.web.servlet.MvcResult;
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
public class UserConfigControllerTest {


    @Autowired
    private WebApplicationContext wac;


    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void after(){
    }

    @Test
    public void selectTest() throws Exception {
//        this.mockMvc.perform(post("/userConfiguration.pinpoint").header("SSO_USER", "naver00000").contentType(MediaType.APPLICATION_JSON)
//                                            .content("{\"favoriteApplications\" :" +
//                                                                        "[" +
//                                                                                "{\"applicationName\":\"A2D-EDC\",\"serviceType\":\"TOMCAT\",\"code\":1010},{\"applicationName\":\"A2D_EDC\",\"serviceType\":\"TOMCAT\",\"code\":1010}"
//                                                                            + "]"
//                                                    +"}"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentType("application/json;charset=UTF-8"))
//                    .andReturn();

        this.mockMvc.perform(put("/userConfiguration.pinpoint").header("SSO_USER", "naver00000").contentType(MediaType.APPLICATION_JSON)
            .content("{\"favoriteApplications\" :" +
                "[" +
                "{\"applicationName\":\"A2D-EDC123\",\"serviceType\":\"TOMCAT\",\"code\":1010},{\"applicationName\":\"A2D_EDC22\",\"serviceType\":\"TOMCAT\",\"code\":1010}"
                + "]"
                +"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andReturn();

        MvcResult mvcResult = this.mockMvc.perform(get("/userConfiguration.pinpoint").header("SSO_USER", "naver00000").contentType(MediaType.APPLICATION_JSON))
                                            .andExpect(status().isOk()).andExpect(content()
                                            .contentType("application/json;charset=UTF-8"))
                                            .andReturn();

        System.out.println("result : " + mvcResult.getResponse().getContentAsString());


//        this.mockMvc.perform(delete("/userConfiguration.pinpoint").header("SSO_USER", "naver00000").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType("application/json;charset=UTF-8"))
//            .andExpect(jsonPath("$", hasKey("result")))
//            .andReturn();
    }
}
