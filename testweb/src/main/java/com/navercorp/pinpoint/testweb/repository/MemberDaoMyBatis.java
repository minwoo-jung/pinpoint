package com.navercorp.pinpoint.testweb.repository;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.testweb.domain.Member;

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
