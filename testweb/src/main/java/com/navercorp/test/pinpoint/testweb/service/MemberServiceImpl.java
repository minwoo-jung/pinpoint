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

import java.util.List;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.repository.MemberDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("memberService")
@Transactional("mysqlTransactionManager")
public class MemberServiceImpl implements MemberService {

    @Autowired
    @Qualifier("memberDaoJdbc")
    private MemberDao dao;

    public MemberDao getDao() {
        return dao;
    }

    public void setDao(MemberDao dao) {
        this.dao = dao;
    }

    public void add(Member member) {
        dao.add(member);
    }

    @Override
    public void addStatement(Member member) {
        dao.addStatement(member);
    }

    public void delete(int id) {
        dao.delete(id);
    }

    public Member get(int id) {
        return dao.get(id);
    }

    public List<Member> list() {
        return dao.list();
    }

    public void update(Member member) {
        dao.update(member);
    }

}
