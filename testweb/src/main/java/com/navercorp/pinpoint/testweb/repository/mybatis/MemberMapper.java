package com.navercorp.pinpoint.testweb.repository.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.testweb.domain.Member;

/**
 * @author Hyun Jeong
 */
@Repository("memberMapper")
public interface MemberMapper {

    Member selectUser(@Param("id") int id);

    List<Member> selectAllUsersInvalid();

    int insertUser(Member member);

    int updateUser(Member member);

    int deleteUser(@Param("id") int id);
}
