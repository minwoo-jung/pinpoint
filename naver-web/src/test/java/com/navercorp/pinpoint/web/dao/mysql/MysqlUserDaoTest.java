/*
 * Copyright 2014 NAVER Corp.
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
 */
package com.navercorp.pinpoint.web.dao.mysql;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web-naver.xml")
@Transactional
public class MysqlUserDaoTest {
    
    @Autowired
    MysqlUserDao dao;
    
    @Test
    public void insertAndDelete() {
        User user = new User("testId", "TEST", "PINPOINT_TEST", "010", "pinpoint@navercorp.com");
        dao.insertUser(user);
        User selectedUser = dao.selectUserByUserId("testId");
        assertEquals(user.getUserId(), selectedUser.getUserId());
        dao.deleteUser(user);
    }
    
    @Test
    public void update() {
        User user = new User("testId", "TEST", "PINPOINT_TEST", "010", "pinpoint@navercorp.com");
        dao.insertUser(user);
        user = new User("testId", "TEST", "PINPOINT_TEST_TEAM", "010", "pinpoint@navercorp.com");
        dao.updateUser(user);
        User selectedUser = dao.selectUserByUserId("testId");
        assertEquals(user.getDepartment(), selectedUser.getDepartment());
    }
    
    @Test
    public void select() {
        User user1 = new User("testId1", "test_user", "PINPOINT_TEST", "010", "pinpoint@navercorp.com");
        User user2 = new User("testId2", "test_user", "PINPOINT_TEST", "010", "pinpoint@navercorp.com");
        
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        
        dao.insertUserList(users);
        assertNotEquals(0, dao.selectUser().size());
        assertEquals(2, dao.selectUserByDepartment("PINPOINT_TEST").size());
        assertNotNull(dao.selectUserByUserId("testId1"));
        assertEquals(2, dao.selectUserByUserName("test_user").size());
    }
}
