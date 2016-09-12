/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.repository.MemberDao;
import com.navercorp.test.pinpoint.testweb.service.MemberServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class MemberServiceImplTest {

    MemberServiceImpl memberService;
    @Mock
    MemberDao dao;

    @Before
    public void setUp() {
        memberService = new MemberServiceImpl();
        memberService.setDao(dao);
    }

    @Test
    public void mockup() {
        assertThat(memberService.getDao(), is(notNullValue()));
    }

    /**
     * http://mockito.googlecode.com/svn/tags/latest/javadoc/org/mockito/Mockito
     * .html
     */
    @Test
    public void add() {
        Member member = new Member();
        member.setId(1);
        member.setName("keesun");
        memberService.add(member);
        verify(dao).add(member);
    }

}
