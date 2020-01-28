/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.service.http;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.ApacheClosableAsyncHttpClient;

/**
 * 
 * @author netspider
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-testweb.xml", "classpath:servlet-context.xml" })
public class ApacheClosableAsyncHttpClientTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApacheClosableAsyncHttpClient apacheClosableAsyncHttpClient;

    @Test
    public void requestPost() {
        String requestPost = apacheClosableAsyncHttpClient.post();
        logger.debug(requestPost);
    }
}