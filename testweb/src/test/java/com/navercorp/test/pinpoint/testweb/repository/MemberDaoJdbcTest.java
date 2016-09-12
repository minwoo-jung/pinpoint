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

package com.navercorp.test.pinpoint.testweb.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.test.pinpoint.testweb.DBUnitSupport;
import com.navercorp.test.pinpoint.testweb.domain.Member;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext-testweb.xml", "/servlet-context.xml" })
@Transactional
public class MemberDaoJdbcTest extends DBUnitSupport {

    @Autowired
    MemberDaoJdbc memberDao;

    @Test
    public void di() {
        assertThat(memberDao, is(notNullValue()));
    }

    @Test
    public void crud() {
        Member member = new Member();
        member.setId(1);
        member.setName("netspider");
        member.setJoined(new Date());
        memberDao.add(member);
        assertThat(memberDao.list().size(), is(1));

        member.setName("chisu");
        memberDao.update(member);
        assertThat(memberDao.get(1).getName(), is("chisu"));

        memberDao.delete(1);
        assertThat(memberDao.list().size(), is(0));
    }

    @Test
    public void searchByName() {
        insertXmlData("/testData.xml");
        assertThat(memberDao.list().size(), is(2));
    }

}
