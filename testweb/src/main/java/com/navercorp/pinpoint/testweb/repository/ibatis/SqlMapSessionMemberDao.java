package com.navercorp.pinpoint.testweb.repository.ibatis;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.testweb.domain.Member;
import com.navercorp.pinpoint.testweb.repository.MemberDao;

/**
 * @author Hyun Jeong
 */
@Repository("sqlMapSessionMemberDao")
public class SqlMapSessionMemberDao implements MemberDao {

    @Autowired
    @Qualifier("mysqlSqlMapClientTemplate")
    private SqlSessionTemplate sqlMapClientTemplate;

    @Override
    public void add(Member member) {
        this.sqlMapClientTemplate.insert("add", member);
    }

    @Override
    public void addStatement(Member member) {
        this.sqlMapClientTemplate.insert("addStatement", member);
    }

    @Override
    public void update(Member member) {
        this.sqlMapClientTemplate.update("update", member);
    }

    @Override
    public Member get(int id) {
        return (Member)this.sqlMapClientTemplate.selectOne("get", id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Member> list() {
        return this.sqlMapClientTemplate.selectList("list");
    }

    @Override
    public void delete(int id) {
        this.sqlMapClientTemplate.delete("delete", id);
    }

}
