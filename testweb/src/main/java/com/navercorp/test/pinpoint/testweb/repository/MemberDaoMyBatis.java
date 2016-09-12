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

import java.util.List;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDaoMyBatis implements MemberDao {

    @Autowired
    @Qualifier("mysqlSqlMapClientTemplate")
    private SqlSessionTemplate sqlMapClientTemplate;

    public void add(Member member) {
        sqlMapClientTemplate.insert("add", member);
    }

    @Override
    public void addStatement(Member member) {
        sqlMapClientTemplate.insert("addStatement", member);
    }

    public void delete(int id) {
        sqlMapClientTemplate.delete("delete", id);
    }

    public Member get(int id) {
        return (Member) sqlMapClientTemplate.selectOne("get", id);
    }

    @SuppressWarnings("unchecked")
    public List<Member> list() {
        return sqlMapClientTemplate.selectList("list");
    }

    public void update(Member member) {
        sqlMapClientTemplate.update("update", member);
    }

}
