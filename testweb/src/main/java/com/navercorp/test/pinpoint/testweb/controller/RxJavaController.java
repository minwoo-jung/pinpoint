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

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.service.RxJavaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rx.Completable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
@Controller
@RequestMapping("/rx")
public class RxJavaController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Random RANDOM = new Random();

    @Autowired
    private RxJavaService service;

    @RequestMapping("/crud")
    @ResponseBody
    public String crud() {
        final int id = RANDOM.nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        Completable crud = service.add(member)
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        service.list()
                                .subscribeOn(Schedulers.io())
                                .forEach(new Action1<Member>() {
                                    @Override
                                    public void call(Member member) {
                                        service.delete(member.getId()).await();
                                    }
                                });
                    }
                });
        boolean successful = false;
        try {
            successful = crud.subscribeOn(Schedulers.io()).await(10000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Error in crud", e);
        }
        if (successful) {
            return "OK";
        } else {
            return "ERROR";
        }
    }
}
