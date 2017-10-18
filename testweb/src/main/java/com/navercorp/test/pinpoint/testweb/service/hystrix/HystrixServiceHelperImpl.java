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

package com.navercorp.test.pinpoint.testweb.service.hystrix;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
@Component
public class HystrixServiceHelperImpl implements HystrixServiceHelper {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Random random = new Random();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    @Qualifier("memberService")
    private MemberService memberService;

    @PreDestroy
    public void preDestroy() {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public List<Integer> initTestData(int memberCount) {
        if (memberCount < 1) {
            throw new IllegalArgumentException("memberCount must be greater than 0");
        }
        List<Integer> insertedMemberIds = new ArrayList<>(memberCount);
        for (int i = 0; i < memberCount; i++) {
            final int id = random.nextInt();
            final Member member = new Member();
            member.setId(id);
            member.setName("Hystrix_" + i);
            member.setJoined(new Date());

            // executing over another thread to not exclude these from the call stack
            Future<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    memberService.add(member);
                    return member.getId();
                }
            });
            try {
                insertedMemberIds.add(future.get(5000L, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                logger.error("Failed to insert user.", e);
            }
        }
        return insertedMemberIds;
    }

    @Override
    public void deleteTestData(List<Integer> memberIds) {
        if (memberIds == null) {
            return;
        }
        for (int memberId : memberIds) {
            // executing over another thread to not exclude these from the call stack
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    memberService.delete(memberId);
                }
            });
        }
    }
}
