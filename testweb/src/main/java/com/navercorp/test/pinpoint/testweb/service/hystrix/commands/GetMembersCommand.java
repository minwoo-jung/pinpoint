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
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class GetMembersCommand extends HystrixCommand<List<Member>> {

    private static final String COMMAND_GROUP = "GetMembers";

    private final HystrixMemberService hystrixMemberService;
    private final List<Integer> memberIds;

    public GetMembersCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        this(hystrixMemberService, memberIds, COMMAND_GROUP, HystrixCommandProperties.defaultSetter());
    }

    protected GetMembersCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds, String commandGroup, HystrixCommandProperties.Setter setter) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup)).andCommandPropertiesDefaults(setter));
        this.hystrixMemberService = hystrixMemberService;
        this.memberIds = memberIds;
    }

    @Override
    public final List<Member> run() throws Exception {
        return getMembers(hystrixMemberService, memberIds);
    }

    protected List<Member> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) throws Exception {
        List<Member> members = new ArrayList<>(memberIds.size());
        for (int memberId : memberIds) {
            Member member = hystrixMemberService.get(memberId);
            members.add(member);
        }
        return members;
    }

    @Override
    protected List<Member> getFallback() {
        return Collections.emptyList();
    }
}
