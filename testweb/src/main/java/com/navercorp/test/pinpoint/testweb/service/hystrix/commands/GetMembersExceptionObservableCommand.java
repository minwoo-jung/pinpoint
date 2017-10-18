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
import rx.Observable;
import rx.Subscriber;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class GetMembersExceptionObservableCommand extends GetMembersObservableCommand {

    private static final String COMMAND_GROUP = "GetMembersExceptionObservable";

    public GetMembersExceptionObservableCommand(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        super(hystrixMemberService, memberIds, COMMAND_GROUP, HystrixCommandProperties.defaultSetter());
    }

    @Override
    protected Observable<Member> constructGetMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        return Observable.range(0, memberIds.size())
                .concatMap(i -> {
                    int memberId = memberIds.get(i);
                    if (i == memberIds.size() - 1) {
                        return Observable.create(new Observable.OnSubscribe<Member>() {
                            @Override
                            public void call(Subscriber<? super Member> subscriber) {
                                try {
                                    if (!subscriber.isUnsubscribed()) {
                                        Member member = hystrixMemberService.getAndThrowException(memberId, new RuntimeException("expected"));
                                        subscriber.onNext(member);
                                        subscriber.onCompleted();
                                    }
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    } else {
                        return Observable.create(new Observable.OnSubscribe<Member>() {
                            @Override
                            public void call(Subscriber<? super Member> subscriber) {
                                try {
                                    if (!subscriber.isUnsubscribed()) {
                                        Member member = hystrixMemberService.get(memberId);
                                        subscriber.onNext(member);
                                        subscriber.onCompleted();
                                    }
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                });
    }
}
