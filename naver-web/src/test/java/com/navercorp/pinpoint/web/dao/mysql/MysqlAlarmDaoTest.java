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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web.xml")
@Transactional
public class MysqlAlarmDaoTest {
	
	@Autowired
	MysqlAlarmDao dao;
	
	@Test
	public void insertAndDelete() {
		Rule rule = new Rule("applicationId", "serviceType", CheckerCategory.ERROR_COUNT.getName(), 5, "userGroupId", true, true, "");
		String ruleId = dao.insertRule(rule);
		rule.setRuleId(ruleId);
		
		Assert.assertEquals(1, dao.selectRuleByApplicationId("applicationId").size());
		
		dao.deleteRule(rule);
		
		Assert.assertEquals(0, dao.selectRuleByApplicationId("applicationId").size());
	}
	
	@Test
	public void deleteRuleByUserGroupId() {
		Rule rule = new Rule("applicationId", "serviceType", CheckerCategory.ERROR_COUNT.getName(), 5, "userGroupId", true, true, "");
		String ruleId = dao.insertRule(rule);
		rule.setRuleId(ruleId);
		
		Assert.assertEquals(1, dao.selectRuleByApplicationId("applicationId").size());
		
		dao.deleteRuleByUserGroupId("userGroupId");
		
		Assert.assertEquals(0, dao.selectRuleByApplicationId("applicationId").size());
	}
	
	@Test
	public void select() {
		Rule rule = new Rule("applicationId", "serviceType", CheckerCategory.ERROR_COUNT.getName(), 5, "userGroupId", true, true, "");
		dao.insertRule(rule);
		
		Assert.assertEquals(1, dao.selectRuleByApplicationId("applicationId").size());
		Assert.assertEquals(1, dao.selectRuleByUserGroupId("userGroupId").size());
		
		dao.deleteRuleByUserGroupId("userGroupId");
	}
	
	@Test
	public void update() {
		Rule rule = new Rule("applicationId", "serviceType", CheckerCategory.ERROR_COUNT.getName(), 5, "userGroupId", true, true, "");
		String ruleId = dao.insertRule(rule);
		rule.setRuleId(ruleId);
		
		rule.setCheckerName(CheckerCategory.ERROR_RATE.getName());
		dao.updateRule(rule);
		
		rule = dao.selectRuleByApplicationId("applicationId").get(0);
		Assert.assertEquals(CheckerCategory.ERROR_RATE.getName(), rule.getCheckerName());
	}

}
