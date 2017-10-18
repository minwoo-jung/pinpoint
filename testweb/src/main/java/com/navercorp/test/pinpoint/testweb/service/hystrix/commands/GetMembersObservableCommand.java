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
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class GetMembersObservableCommand extends HystrixObservableCommand<Member> {

    private static final String COMMAND_GROUP = "GetMembersObservable";

    private final HystrixMemberService hystrixMemberService;
    private final List<Integer> memberIds;

    public GetMembersObservableCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        this(hystrixMemberService, memberIds, COMMAND_GROUP, HystrixCommandProperties.defaultSetter());
    }

    protected GetMembersObservableCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds, String commandGroup, HystrixCommandProperties.Setter setter) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup)).andCommandPropertiesDefaults(setter));
        this.hystrixMemberService = hystrixMemberService;
        this.memberIds = memberIds;
    }

    @Override
    protected final Observable<Member> construct() {
        return constructGetMembers(hystrixMemberService, memberIds);
    }

    protected Observable<Member> constructGetMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        return Observable.from(memberIds)
                .concatMap(memberId -> {
                    Member member = hystrixMemberService.get(memberId);
                    return Observable.just(member);
                });
    }

    @Override
    protected Observable<Member> resumeWithFallback() {
        return Observable.empty();
    }
}
