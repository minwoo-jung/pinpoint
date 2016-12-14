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
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class BLOC4Controller {

    private final String BLOC3_ECHO = "http://10.110.241.190:5001/welcome/com.nhncorp.lucy.bloc.welcome.EchoBO/execute?foo=bar";
    private final String BLOC4_ECHO = "http://10.110.241.190:15001/welcome/test/hello?foo=bar";

    @RequestMapping(value = "/bloc3/call")
    @ResponseBody
    public String requestGetToBloc3() {
        return HttpUtil.url(BLOC3_ECHO).method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }

    @RequestMapping(value = "/bloc4/call")
    @ResponseBody
    public String requestGetToBloc4() {
        return HttpUtil.url(BLOC4_ECHO).method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }
}
