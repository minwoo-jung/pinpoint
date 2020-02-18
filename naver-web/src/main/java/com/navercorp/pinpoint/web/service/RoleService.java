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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.UserRole;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;

import java.util.List;

/**
 * @author minwoo.jung
 */
public interface RoleService {
    void insertRoleInformation(RoleInformation roleInformation);

    RoleInformation selectRoleInformation(String roleId);

    void deleteRoleInformation(String roleId);

    void updateRoleInformation(RoleInformation roleInformation);

    void insertUserRole(UserRole userRole);

    void deleteUserRole(String userId);

    void updateUserRole(UserRole userRole);

    UserRole selectUserRole(String userId);

    RoleInformation getUserPermission(String userId);

    List<String> selectRoleList();

    void dropAndCreateUserRoleTable();
}