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

import com.navercorp.test.pinpoint.testweb.service.NimmService;
import com.navercorp.test.pinpoint.testweb.util.Description;

import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class NIMMController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NimmService nimmService;

    public NIMMController() throws Exception {
    }

    @Description("invoke and return value.")
    @RequestMapping(value = "/nimm/invokeAndReturnValue/target/bloc3")
    @ResponseBody
    public String invokeAndReturnValueToBloc3() throws Exception {
        nimmService.get("bloc3", null);
        return "OK";
    }

    @Description("invoke and return value.")
    @RequestMapping(value = "/nimm/invokeAndReturnValue/target/bloc4")
    @ResponseBody
    public String invokeAndReturnValue() throws Exception {
        nimmService.get("bloc4", null);
        return "OK";
    }


    @Description("callback is InvocationFutureListener.")
    @RequestMapping(value = "/nimm/invokeAndCallback/target/bloc3")
    @ResponseBody
    public String invokeAndCallabckToBloc3() throws Exception {
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed nimm listen");
            }
        };

        nimmService.get("bloc3", callback);
        return "OK";
    }

    @Description("callback is InvocationFutureListener.")
    @RequestMapping(value = "/nimm/invokeAndCallback/target/bloc4")
    @ResponseBody
    public String invokeAndCallabckToBloc4() throws Exception {
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed nimm listen");
            }
        };

        nimmService.get("bloc4", callback);
        return "OK";
    }
}