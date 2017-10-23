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
import org.springframework.stereotype.Service;

/**
 * @author HyunGil Jeong
 */
@Service
public class HystrixMemberServiceImpl implements HystrixMemberService {

    @Autowired
    @Qualifier("memberService")
    private MemberService memberService;

    @Override
    public Member get(int id) {
        return memberService.get(id);
    }

    @Override
    public Member getAndThrowException(int id, Exception exception) throws Exception {
        if (exception == null) {
            throw new NullPointerException("exception must not be null");
        }
        memberService.get(id);
        throw exception;
    }
}
