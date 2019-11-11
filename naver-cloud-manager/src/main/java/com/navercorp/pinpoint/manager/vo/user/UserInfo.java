/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.manager.vo.user;

import com.navercorp.pinpoint.manager.domain.mysql.repository.user.User;

/**
 * @author HyunGil Jeong
 */
public class UserInfo {

    private String userId;
    private String name;
    private String department;
    private String phoneNumber;
    private String email;

    public static UserInfo fromUser(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.userId = user.getUserId();
        userInfo.name = user.getName();
        userInfo.department = user.getDepartment();
        userInfo.email = user.getEmail();
        return userInfo;
    }

    public static UserInfo fromAdminCreateForm(AdminCreateForm form) {
        UserInfo userInfo = new UserInfo();
        userInfo.userId = form.getUserId();
        userInfo.name = form.getUserName();
        userInfo.department = form.getDepartment();
        userInfo.email = form.getEmail();
        return userInfo;
    }

    public static User toUser(UserInfo userInfo) {
        User user = new User();
        user.setUserId(userInfo.userId);
        user.setName(userInfo.name);
        user.setDepartment(userInfo.department);
        user.setPhoneNumber(userInfo.phoneNumber);
        user.setEmail(userInfo.email);
        return user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
