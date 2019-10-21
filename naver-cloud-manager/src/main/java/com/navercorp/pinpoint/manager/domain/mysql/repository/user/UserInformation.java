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

package com.navercorp.pinpoint.manager.domain.mysql.repository.user;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class UserInformation {

    private User profile;
    private UserAccount account;
    private UserRole role;

    public UserInformation(User profile, UserAccount account, UserRole role) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.account = Objects.requireNonNull(account, "account");
        this.role = Objects.requireNonNull(role, "role");
    }

    public UserInformation(User profile, UserRole role) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.role = Objects.requireNonNull(role, "role");
        this.account = null;
    }

    public UserInformation() {
    }

    public User getProfile() {
        return profile;
    }

    public void setProfile(User profile) {
        this.profile = profile;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserInformation{");
        sb.append("profile=").append(profile);
        sb.append(", account=").append(account);
        sb.append(", role=").append(role);
        sb.append('}');
        return sb.toString();
    }
}
