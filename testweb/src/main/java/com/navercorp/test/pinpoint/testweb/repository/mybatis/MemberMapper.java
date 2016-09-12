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

package com.navercorp.test.pinpoint.testweb.repository.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.navercorp.test.pinpoint.testweb.domain.Member;

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
