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

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.service.MemberService;
import com.navercorp.test.pinpoint.testweb.service.MySqlService;
import com.navercorp.test.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Random;

/**
 *
 */
@Controller
public class MySqlController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MySqlService mySqlService;

    @Autowired
    @Qualifier("memberService")
    private MemberService service;

    private final Random RANDOM = new Random();

    @RequestMapping(value = "/mysql/crud")
    @ResponseBody
    public String crud() {
        int id = RANDOM.nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        service.add(member);
        service.list();
        service.delete(id);

        return "OK";
    }

    @RequestMapping(value = "/mysql/crudWithStatement")
    @ResponseBody
    public String crudWithStatement() {

        int id = RANDOM.nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        service.addStatement(member);
        service.list();
        service.delete(id);

        return "OK";
    }

    @Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mysql/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = mySqlService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";

    }

    @Description("preparedStatement 테스트 w/ 파라미터. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mysql/selectOneWithParam")
    @ResponseBody
    public String selectOneWithParam() {
        logger.info("selectOneWithParam start");

        int id = 99;
        mySqlService.selectOneWithParam(id);

        logger.info("selectOneWithParam end. id:{}", id);
        return "OK";
    }

    @Description("stored procedure IN, OUT 테스트")
    @RequestMapping(value = "/mysql/callConcatCharacters")
    @ResponseBody
    public String callConcatCharacters() {
        logger.info("callConcatCharacters start");
        char a = 'a';
        char b = 'b';
        String concat = mySqlService.concat(a, b);

        logger.info("callConcatCharacters end. concat:{}", concat);
        if ("ab".equals(concat)) {
            return "OK";
        } else {
            return "FAIL";
        }
    }

    @Description("stored procedure INOUT 테스트")
    @RequestMapping(value = "/mysql/callSwapAndGetSum")
    @ResponseBody
    public String callSwapAndGetSum() {
        logger.info("callSwapAndGetSum start");
        int a = 1;
        int b = 2;
        int sum = mySqlService.swapAndGetSum(a, b);

        logger.info("callSwapAndGetSum end. sum:{}", sum);
        if (sum != a + b) {
            return "FAIL";
        } else {
            return "OK";
        }
    }

    @Description("statement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mysql/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        mySqlService.createStatement();

        logger.info("createStatement end:{}");
        return "OK";

    }
}
