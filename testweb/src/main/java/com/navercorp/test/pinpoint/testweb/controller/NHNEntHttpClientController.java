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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class NHNEntHttpClientController {

    @RequestMapping(value = "/nhnent/get")
    @ResponseBody
    public String requestGet(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
        return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }

    @RequestMapping(value = "/nhnent/getWithParam")
    @ResponseBody
    public String requestGetWithParam() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/nhnent/post")
    @ResponseBody
    public String requestPost(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
        return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
    }

    @RequestMapping(value = "/nhnent/postWithBody")
    @ResponseBody
    public String requestPostWithBody() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/nhnent/postWithMultipart")
    public String requestPostWithMultipart() {
        return "NOT_IMPLEMENTED";
    }
}