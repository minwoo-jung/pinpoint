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
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersExceptionCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersShortCircuitCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersTimeoutCommand;
import com.netflix.hystrix.HystrixCommand;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Service("hystrixCommandObserveService")
public class HystrixCommandObserveService extends HystrixObservableService<List<Member>> {

    @Override
    protected Observable<List<Member>> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixCommand<List<Member>> getMembersCommand = new GetMembersCommand(hystrixMemberService, memberIds);
        return getMembersCommand.observe();
    }

    @Override
    protected Observable<List<Member>> getMembersException(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixCommand<List<Member>> getMembersExceptionCommand = new GetMembersExceptionCommand(hystrixMemberService, memberIds);
        return getMembersExceptionCommand.observe();
    }

    @Override
    protected Observable<List<Member>> getMembersTimeout(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixCommand<List<Member>> getMembersTimeoutCommand = new GetMembersTimeoutCommand(hystrixMemberService, memberIds);
        return getMembersTimeoutCommand.observe();
    }

    @Override
    protected Observable<List<Member>> getMembersShortCircuit(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixCommand<List<Member>> getMembersShortCircuitCommand = new GetMembersShortCircuitCommand(hystrixMemberService, memberIds);
        return getMembersShortCircuitCommand.observe();
    }
}
