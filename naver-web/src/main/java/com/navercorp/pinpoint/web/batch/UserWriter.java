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
package com.navercorp.pinpoint.web.batch;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.UserRole;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
public class UserWriter implements ItemWriter<User> {

    private final static List<String> ROLE = new ArrayList<>(1);

    static {
        ROLE.add("user");
    }

    @Autowired
	UserService userService;

    @Autowired
    RoleService roleService;
	
	@Override
	public void write(List<? extends User> users) throws Exception {
		userService.insertUserList((List<User>) users);

        for (User user : users) {
            roleService.insertUserRole(new UserRole(user.getUserId(), ROLE));
        }
	}
}
