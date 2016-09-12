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

package com.navercorp.test.pinpoint.testweb.service.orm.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.repository.mybatis.MemberMapper;
import com.navercorp.test.pinpoint.testweb.service.MemberService;

/**
 * @author Hyun Jeong
 */
@Service("myBatisMemberService")
@Transactional("mysqlTransactionManager")
public class MyBatisMemberService implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public void add(Member member) {
        this.memberMapper.insertUser(member);
    }

    @Override
    public void addStatement(Member member) {
        this.memberMapper.insertUser(member);
    }

    @Override
    public void update(Member member) {
        this.memberMapper.updateUser(member);
    }

    @Override
    public Member get(int id) {
        return this.memberMapper.selectUser(id);
    }

    @Override
    public List<Member> list() {
        return this.memberMapper.selectAllUsersInvalid();
    }

    @Override
    public void delete(int id) {
        this.memberMapper.deleteUser(id);
    }

}
