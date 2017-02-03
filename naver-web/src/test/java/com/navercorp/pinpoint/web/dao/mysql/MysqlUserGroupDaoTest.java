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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web-naver.xml")
@Transactional
public class MysqlUserGroupDaoTest {
   
    @Autowired
    MysqlUserGroupDao dao;
    
    @Autowired
    MysqlUserDao userDao;
    
    private User user1 = new User("userId1", "testName", "PINPOINT", "0101", "pinpoint1@navercorp.com");
    private User user2 = new User("userId2", "testName", "PINPOINT", "0102", "pinpoint2@navercorp.com");
    
    @Before
    public void before() {
        userDao.insertUser(user1);
        userDao.insertUser(user2);
    }
    
    @After
    public void after() {
        userDao.deleteUser(user1);
        userDao.deleteUser(user2);
    }
   
    @Test
    public void createAndDeleteUserGroup() {
        UserGroup group = new UserGroup("", "test_group");
        dao.createUserGroup(group);
        int before = dao.selectUserGroup().size();
        dao.deleteUserGroup(group);
        int after = dao.selectUserGroup().size();
        Assert.assertEquals(after, before - 1);
    }
    
    @Test
    public void updateUsergroup() {
        UserGroup group = new UserGroup("", "test_group");
        group.setNumber(dao.createUserGroup(group));
        
        group.setId("test_group_update");
        dao.updateUserGroup(group);
        dao.deleteUserGroup(group);
    }
    
    @Test
    public void insertAndDeleteMember() {
        UserGroupMember member1 = new UserGroupMember("test_group", "userId1");
        UserGroupMember member2 = new UserGroupMember("test_group", "userId2");
        dao.insertMember(member1);
        dao.insertMember(member2);
        Assert.assertEquals(2, dao.selectMember("test_group").size());
        
        dao.deleteMember(member1);
        dao.deleteMember(member2);
        Assert.assertEquals(0, dao.selectMember("test_group").size());
    }
  
    @Test
    public void deleteMemberByUserGroupId() {
        UserGroupMember member = new UserGroupMember("test_group", "userId1");
        
        dao.insertMember(member);
        Assert.assertEquals(1, dao.selectMember("test_group").size());
        
        dao.deleteMemberByUserGroupId("test_group");
        Assert.assertEquals(0, dao.selectMember("test_group").size());
    }
    
    @Test
    public void updateUserGroupIdOfMember() {
        UserGroup group = new UserGroup("", "test_group");
        group.setId(dao.createUserGroup(group));
        
        User user = new User("userId", "testName", "PINPOINT", "010", "pinpoint1@navercorp.com");
        userDao.insertUser(user);
        UserGroupMember member = new UserGroupMember("test_group", "userId");
        dao.insertMember(member);
        
        group.setId("test_group_update");
        dao.updateUserGroupIdOfMember(group);
        Assert.assertEquals(1, dao.selectMember("test_group_update").size());

        userDao.deleteUser(user);
        dao.deleteUserGroup(group);
    }
    
    @Test
    public void selectUserGroupByUserId() {
        UserGroup group1 = new UserGroup("", "test_group1");
        UserGroup group2 = new UserGroup("", "test_group2");
        dao.createUserGroup(group1);
        dao.createUserGroup(group2);
        
        UserGroupMember member1 = new UserGroupMember("test_group1", "userId1");
        UserGroupMember member2 = new UserGroupMember("test_group2", "userId1");
        dao.insertMember(member1);
        dao.insertMember(member2);

        Assert.assertEquals(2, dao.selectUserGroupByUserId("userId1").size());
        
        dao.deleteMember(member1);
        dao.deleteMember(member2);
        dao.deleteUserGroup(group1);
        dao.deleteUserGroup(group2);
    }
    
    @Test
    public void selectInformationOfMember() {
        UserGroupMember member1 = new UserGroupMember("test_group", "userId1");
        UserGroupMember member2 = new UserGroupMember("test_group", "userId2");
        dao.insertMember(member1);
        dao.insertMember(member2);
        
        Assert.assertEquals(2, dao.selectPhoneNumberOfMember("test_group").size());
        Assert.assertEquals(2, dao.selectEmailOfMember("test_group").size());
      
        dao.deleteMember(member1);
        dao.deleteMember(member2);
    }
}
