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

package com.navercorp.test.pinpoint.testweb.service;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rx.Completable;
import rx.CompletableSubscriber;
import rx.Observable;

/**
 * @author HyunGil Jeong
 */
@Service
public class RxJavaServiceImpl implements RxJavaService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("memberService")
    private MemberService service;

    @Override
    public Completable add(Member member) {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(CompletableSubscriber completableSubscriber) {
                try {
                    service.add(member);
                    logger.info("added member id : {}", member.getId());
                    completableSubscriber.onCompleted();
                } catch (Exception e) {
                    completableSubscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Member> list() {
        return Observable.from(service.list());
    }

    @Override
    public Completable delete(int id) {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(CompletableSubscriber completableSubscriber) {
                try {
                    service.delete(id);
                    logger.info("deleted member id : {}", id);
                    completableSubscriber.onCompleted();
                } catch (Exception e) {
                    completableSubscriber.onError(e);
                }
            }
        });
    }
}
