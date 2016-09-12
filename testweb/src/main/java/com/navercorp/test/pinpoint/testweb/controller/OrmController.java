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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author Hyun Jeong
 */
@Controller
public class OrmController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String MYBATIS_VIEW = "orm/mybatis";

    @Autowired
    @Qualifier("myBatisMemberService")
    private MemberService myBatisMemberService;

    @RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/query")
    public String myBatisSqlSessionTemplateQuery() {
        logger.info("/orm/mybatis/sqlSessionTemplate/query");

        this.myBatisMemberService.get(0);

        return MYBATIS_VIEW;
    }

    @RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/transaction")
    public String myBatisSqlSessionTemplateTransaction() {
        logger.info("/orm/mybatis/sqlSessionTemplate/transaction");

        runTransaction(this.myBatisMemberService);

        return MYBATIS_VIEW;
    }

    @RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/invalid")
    public String myBatisSqlSessionTemplateInvalid() {
        logger.info("/orm/mybatis/sqlSessionTemplate/invalid");

        this.myBatisMemberService.list();

        return MYBATIS_VIEW;
    }

    private void runTransaction(MemberService memberService) {

        final int memberId = 1574;

        Member member = new Member();
        member.setId(memberId);
        member.setName("test User");
        member.setJoined(new Date(System.currentTimeMillis()));

        memberService.add(member);

        member.setName("updated test User");
        memberService.update(member);

        memberService.get(memberId);
        logger.info("\tId:[" + member.getId() + "], name:[" + member.getName() + "]");

        memberService.delete(memberId);
    }
}
