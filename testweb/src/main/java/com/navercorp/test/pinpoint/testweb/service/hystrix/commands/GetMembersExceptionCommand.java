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

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class GetMembersExceptionCommand extends GetMembersCommand {

    private static final String COMMAND_GROUP = "GetMembersException";

    public GetMembersExceptionCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        super(hystrixMemberService, memberIds, COMMAND_GROUP, HystrixCommandProperties.defaultSetter());
    }

    @Override
    protected List<Member> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) throws Exception {
        List<Member> members = new ArrayList<>(memberIds.size());
        for (int i = 0; i < memberIds.size(); i++) {
            int memberId = memberIds.get(i);
            if (i == memberIds.size() - 1) {
                members.add(hystrixMemberService.getAndThrowException(memberId, new RuntimeException("expected")));
            } else {
                members.add(hystrixMemberService.get(memberId));
            }
        }
        return members;
    }
}
