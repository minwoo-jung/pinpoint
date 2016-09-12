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

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class BLOC4Controller {

    private final String LOCAL_BLOC4_FORMAT = "http://%s:%s/welcome/test/hello?name=netspider";

    @RequestMapping(value = "/bloc4/callLocal")
    @ResponseBody
    public String requestGet(
            @RequestParam(required = false, defaultValue = "localhost") String host,
            @RequestParam(required = false, defaultValue = "5001") String port) {
        return HttpUtil.url(String.format(LOCAL_BLOC4_FORMAT, host, port)).method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }
}
