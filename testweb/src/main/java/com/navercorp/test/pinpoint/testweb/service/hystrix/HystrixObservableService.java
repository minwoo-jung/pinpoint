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

import com.navercorp.test.pinpoint.testweb.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import rx.Observable;
import rx.functions.Action0;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class HystrixObservableService<T> implements HystrixService<Observable<T>> {

    private static final int TEST_MEMBER_COUNT = 2;

    @Autowired
    @Qualifier("memberService")
    private MemberService memberService;

    @Autowired
    private HystrixMemberService hystrixMemberService;

    @Autowired
    private HystrixServiceHelper hystrixServiceHelper;

    @Override
    public Observable<T> getMembers() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        return getMembers(hystrixMemberService, memberIds)
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        hystrixServiceHelper.deleteTestData(memberIds);
                    }
                });
    }

    @Override
    public Observable<T> getMembersException() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        return getMembersException(hystrixMemberService, memberIds)
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        hystrixServiceHelper.deleteTestData(memberIds);
                    }
                });
    }

    @Override
    public Observable<T> getMembersTimeout() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        return getMembersTimeout(hystrixMemberService, memberIds)
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        hystrixServiceHelper.deleteTestData(memberIds);
                    }
                });
    }

    @Override
    public Observable<T> getMembersShortCircuit() {
        List<Integer> memberIds = hystrixServiceHelper.initTestData(TEST_MEMBER_COUNT);
        return getMembersShortCircuit(hystrixMemberService, memberIds)
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        hystrixServiceHelper.deleteTestData(memberIds);
                    }
                });
    }

    protected abstract Observable<T> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract Observable<T> getMembersException(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract Observable<T> getMembersTimeout(HystrixMemberService hystrixMemberService, List<Integer> memberIds);

    protected abstract Observable<T> getMembersShortCircuit(HystrixMemberService hystrixMemberService, List<Integer> memberIds);
}
