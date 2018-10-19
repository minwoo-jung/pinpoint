/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class UserRole {

    private String userId;
    private List<String> roleList;

    public UserRole() {
    }

    public UserRole(String userId, List<String> roleList) {
        Assert.hasText(userId, "userId must not be empty");
        this.userId = userId;
        this.roleList = Objects.requireNonNull(roleList, "roleList must not be null");;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<String> roleList) {
        this.roleList = roleList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserRole{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", RoleList=").append(roleList);
        sb.append('}');
        return sb.toString();
    }
}
