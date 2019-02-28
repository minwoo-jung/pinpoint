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

import com.navercorp.pinpoint.web.dao.RoleDao;
import com.navercorp.pinpoint.web.vo.UserRole;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public void insertRoleInformation(RoleInformation roleInformation) {
        roleDao.insertRoleInformation(roleInformation);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleInformation selectRoleInformation(String roleId) {
        return roleDao.selectRoleInformation(roleId);
    }

    @Override
    public void deleteRoleInformation(String roleId) {
        roleDao.deleteRoleInformation(roleId);
    }

    @Override
    public void updateRoleInformation(RoleInformation roleInformation) {
        roleDao.updateRoleInformation(roleInformation);
    }

    @Override
    public void insertUserRole(UserRole userRole) {
        roleDao.insertUserRole(userRole);
    }

    @Override
    public void deleteUserRole(String userId) {
        roleDao.deleteUserRole(userId);
    }

    @Override
    public void updateUserRole(UserRole userRole) {
        roleDao.deleteUserRole(userRole.getUserId());
        roleDao.insertUserRole(userRole);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRole selectUserRole(String userId) {
        return roleDao.selectUserRole(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleInformation getUserPermission(String userId) {
        UserRole userRole = roleDao.selectUserRole(userId);

        List<String> roleList = userRole.getRoleList();
        if (roleList.isEmpty()) {
            return RoleInformation.UNASSIGNED_ROLE;
        }

        List<RoleInformation> roleInformationList = new ArrayList<>(roleList.size());
        for (String roleId : roleList) {
            RoleInformation roleInformation = roleDao.selectRoleInformation(roleId);

            if (Objects.nonNull(roleInformation)) {
                roleInformationList.add(roleInformation);
            }
        }

        if (roleInformationList.isEmpty()) {
            return RoleInformation.UNASSIGNED_ROLE;
        }

        return mergeRoleInformation(roleInformationList);
    }

    @Override
    public List<String> selectRoleList() {
        return roleDao.selectRoleList();
    }

    @Override
    public void dropAndCreateUserRoleTable() {
        roleDao.dropAndCreateUserRoleTable();
    }

    private RoleInformation mergeRoleInformation(List<RoleInformation> roleInformationList) {
        final int listSize = roleInformationList.size();

        if (listSize == 1) {
            return roleInformationList.get(0);
        }

        RoleInformation mergedRoleInformation = roleInformationList.get(0);

        for (int index = 1; index < listSize; index++) {
            mergedRoleInformation = RoleInformation.merge(mergedRoleInformation, roleInformationList.get(index));
        }

        return mergedRoleInformation;
    }


}
