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

package com.navercorp.test.pinpoint.testweb.connector.apachehttp4.nhnent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;
import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtilException;

/**
 * 
 * @author netspider
 * 
 */
public class HttpUtilTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String URL = "http://www.naver.com/";

    @Test
    public void callUrl() {
        try {
            String response = HttpUtil.url(URL).method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
            logger.debug(response);
        } catch (HttpUtilException e) {
        }
    }

}
