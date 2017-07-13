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

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.service.PostgreSqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author HyunGil Jeong
 */
@Controller
public class PostgreSqlController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PostgreSqlService postgreSqlService;

    @RequestMapping("/postgreSql/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = postgreSqlService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";
    }

    @RequestMapping(value = "/postgreSql/selectOneWithParam")
    @ResponseBody
    public String selectOneWithParam() {
        logger.info("selectOneWithParam start");

        int id = 99;
        postgreSqlService.selectOneWithParam(id);

        logger.info("selectOneWithParam end. id:{}", id);
        return "OK";
    }

    @RequestMapping(value = "/postgreSql/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        postgreSqlService.createStatement();

        logger.info("createStatement end:{}");
        return "OK";
    }
}
