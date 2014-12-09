package com.navercorp.pinpoint.testweb.repository;

import java.util.List;

import com.navercorp.pinpoint.testweb.domain.Member;

public interface MemberDao {

	void add(Member member);

    void addStatement(Member member);

	void update(Member member);

	Member get(int id);

	List<Member> list();

	void delete(int id);

}
