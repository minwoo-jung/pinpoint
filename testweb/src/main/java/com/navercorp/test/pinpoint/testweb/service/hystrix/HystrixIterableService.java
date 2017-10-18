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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class HystrixIterableService implements HystrixService<List<Member>> {

    private static final int TEST_MEMBER_COUNT = 2;

    @Autowired
    @Qualifier("memberService")
    private MemberService memberService;

    @Autowired
    private HystrixMemberService hystrixMemberService;

    @Autowired
    private HystrixServiceHelper hystrixServiceHelper;

    @Override
    public List<Member> getMembers() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        try {
            return getMembers(hystrixMemberService, memberIds);
        } finally {
            hystrixServiceHelper.deleteTestData(memberIds);
        }
    }

    @Override
    public List<Member> getMembersException() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        try {
            return getMembersException(hystrixMemberService, memberIds);
        } finally {
            hystrixServiceHelper.deleteTestData(memberIds);
        }
    }

    @Override
    public List<Member> getMembersTimeout() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        try {
            return getMembersTimeout(hystrixMemberService, memberIds);
        } finally {
            hystrixServiceHelper.deleteTestData(memberIds);
        }
    }

    @Override
    public List<Member> getMembersShortCircuit() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        try {
            return getMembersShortCircuit(hystrixMemberService, memberIds);
        } finally {
            hystrixServiceHelper.deleteTestData(memberIds);
        }
    }

    protected abstract List<Member> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract List<Member> getMembersException(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract List<Member> getMembersTimeout(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract List<Member> getMembersShortCircuit(HystrixMemberService hystrixMemberService, List<Integer> memberIds);
}
