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

package com.navercorp.test.pinpoint.testweb.service.hystrix.commands;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.service.hystrix.HystrixMemberService;
import com.netflix.hystrix.HystrixCommandProperties;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class GetMembersTimeoutCommand extends GetMembersCommand {

    private static final String COMMAND_GROUP = "GetMembersTimeout";

    public static final int TIMEOUT_MS = 1000;

    public GetMembersTimeoutCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        super(hystrixMemberService, memberIds, COMMAND_GROUP, HystrixCommandProperties.defaultSetter().withExecutionTimeoutInMilliseconds(TIMEOUT_MS));
    }

    @Override
    protected List<Member> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) throws Exception {
        List<Member> members = super.getMembers(hystrixMemberService, memberIds);
        try {
            Thread.sleep(TIMEOUT_MS * 2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return members;
    }
}
