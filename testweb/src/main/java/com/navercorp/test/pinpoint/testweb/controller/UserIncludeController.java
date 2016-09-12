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

import com.test.include.StandAloneCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Naver on 2015-11-27.
 */
@Controller
public class UserIncludeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/include/standalone/execute")
    @ResponseBody
    public String execute() {
        logger.info("execute");

        StandAloneCase testcase = new StandAloneCase();
        return testcase.execute();
    }

    @RequestMapping(value="/include/standalone/nested")
    @ResponseBody
    public String nested() {
        logger.info("nested");

        StandAloneCase testcase = new StandAloneCase();
        return testcase.nested();
    }
}
