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
import com.navercorp.pinpoint.manager.validator.constraint.UserIdConstraint;
import com.navercorp.pinpoint.manager.validator.constraint.UserPasswordConstraint;

/**
 * @author HyunGil Jeong
 */
public class AdminCreateForm {

    @UserIdConstraint
    private String userId;

    @UserPasswordConstraint
    private String password;

    // TODO username validation
    private String userName;

    // TODO department validation
    private String department;

    // TODO email validation
    private String email;

    public User toUser() {
        User user = new User();
        user.setUserId(userId);
        user.setName(userName);
        user.setDepartment(department);
        user.setEmail(email);
        return user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
