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
package com.navercorp.pinpoint.web.dao.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.RoleDao;
import com.navercorp.pinpoint.web.vo.UserRole;
import com.navercorp.pinpoint.web.vo.role.PermissionCollection;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlRoleDao implements RoleDao {
    private static final String NAMESPACE = RoleDao.class.getPackage().getName() + "." + RoleDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<String> selectRoleList() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectRoleList");
    }

    @Override
    public void dropAndCreateUserRoleTable() {
        sqlSessionTemplate.selectList(NAMESPACE + "dropAndCreateUserRoleTable");
    }

    @Override
    public void insertRoleInformation(RoleInformation roleInformation) {
        RoleInfoConvertedJson roleInfo = new RoleInfoConvertedJson(roleInformation);
        sqlSessionTemplate.insert(NAMESPACE + "insertRoleInformation", roleInfo);
    }

    @Override
    public RoleInformation selectRoleInformation(String roleId) {
        RoleInfoConvertedJson roleInfoConvertedJson = sqlSessionTemplate.selectOne(NAMESPACE + "selectRoleInformation", roleId);

        if (Objects.isNull(roleInfoConvertedJson)) {
            return null;
        }

        return roleInfoConvertedJson.convertRoleInformation();
    }

    @Override
    public void deleteRoleInformation(String roleId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteRoleInformation", roleId);
    }

    @Override
    public void updateRoleInformation(RoleInformation roleInformation) {
        RoleInfoConvertedJson roleInfo = new RoleInfoConvertedJson(roleInformation);
        sqlSessionTemplate.update(NAMESPACE + "updateRoleInformation", roleInfo);
    }

    @Override
    public void insertUserRole(UserRole userRole) {
        List<String> roleList = userRole.getRoleList();

        if (CollectionUtils.isEmpty(roleList)) {
            return;
        }

        String userId = userRole.getUserId();
        for (String roleId : roleList) {
            sqlSessionTemplate.insert("insertUserRole", new UserRoleInfo(userId, roleId));
        }
    }

    @Override
    public void deleteUserRole(String userId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUserRole", userId);
    }

    @Override
    public UserRole selectUserRole(String userId) {
        List<String> roleList = sqlSessionTemplate.selectList(NAMESPACE + "selectUserRole", userId);
        return new UserRole(userId, roleList);
    }

    public static class RoleInfoConvertedJson {

        private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private String roleId;

        private String permissionCollectionJson;

        public RoleInfoConvertedJson() {
        }

        public RoleInfoConvertedJson(RoleInformation roleInformation) {
            Objects.requireNonNull(roleInformation, "roleInformation");

            this.roleId = roleInformation.getRoleId();
            try {
                this.permissionCollectionJson = OBJECT_MAPPER.writeValueAsString(roleInformation.getPermissionCollection());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("can not covert permissionCollection to json :" + roleInformation.getPermissionCollection(), e);
            }
        }

        public RoleInformation convertRoleInformation() {
            String roleId = this.roleId;
            try {
                PermissionCollection permissionCollection = OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(permissionCollectionJson, PermissionCollection.class);
                return new RoleInformation(roleId, permissionCollection);
            } catch (IOException e) {
                throw new RuntimeException("can not covert permissionCollectionJson to permissionCollection :" + permissionCollectionJson, e);
            }
        }

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getPermissionCollectionJson() {
            return permissionCollectionJson;
        }

        public void setPermissionCollectionJson(String permissionCollectionJson) {
            this.permissionCollectionJson = permissionCollectionJson;
        }
    }

    public static class UserRoleInfo {

        private String userId;
        private String roleId;

        public UserRoleInfo(String userId, String roleId) {
            Assert.hasText(userId, "userId must not be empty");
            Assert.hasText(roleId, "roleId must not be empty");
            this.userId = userId;
            this.roleId = roleId;
        }

        public UserRoleInfo () {
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }
    }
}
