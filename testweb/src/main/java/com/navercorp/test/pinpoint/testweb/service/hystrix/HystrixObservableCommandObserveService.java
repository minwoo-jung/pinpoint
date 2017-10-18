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
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersExceptionObservableCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersObservableCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersShortCircuitObservableCommand;
import com.navercorp.test.pinpoint.testweb.service.hystrix.commands.GetMembersTimeoutObservableCommand;
import com.netflix.hystrix.HystrixObservableCommand;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Service("hystrixObservableCommandObserveService")
public class HystrixObservableCommandObserveService extends HystrixObservableService<Member> {

    @Override
    protected Observable<Member> getMembers(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixObservableCommand<Member> getMembersObservableCommand = new GetMembersObservableCommand(hystrixMemberService, memberIds);
        return getMembersObservableCommand.observe();
    }

    @Override
    protected Observable<Member> getMembersException(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixObservableCommand<Member> getMembersObservableCommand = new GetMembersExceptionObservableCommand(hystrixMemberService, memberIds);
        return getMembersObservableCommand.observe();
    }

    @Override
    protected Observable<Member> getMembersTimeout(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixObservableCommand<Member> getMembersObservableCommand = new GetMembersTimeoutObservableCommand(hystrixMemberService, memberIds);
        return getMembersObservableCommand.observe();
    }

    @Override
    protected Observable<Member> getMembersShortCircuit(HystrixMemberService hystrixMemberService, List<Integer> memberIds) {
        HystrixObservableCommand<Member> getMembersObservableCommand = new GetMembersShortCircuitObservableCommand(hystrixMemberService, memberIds);
        return getMembersObservableCommand.observe();
    }
}
