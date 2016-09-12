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

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;

/**
 * @author emeroad
 */
@Controller
public class ExcludeUrlController {
    @RequestMapping(value = "/excludeURL")
    @ResponseBody
    public String excludeUrl(HttpServletRequest request) {

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + request.getLocalPort() + "/monitor/l7check.html", new HashMap());
        return "OK";
    }

    @RequestMapping(value = "/nonExcludeURL")
    @ResponseBody
    public String nonExcludeURL(HttpServletRequest request) {

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + request.getLocalPort() + "/", new HashMap());
        return "OK";
    }

}
