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
package com.navercorp.pinpoint.web.vo.role;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class RoleInformation {

    public final static RoleInformation UNASSIGNED_ROLE = new RoleInformation("Unassined_role", PermissionCollection.DEFAULT);

    private String roleId;
    private PermissionCollection permissionCollection;

    public RoleInformation(String roleId, PermissionCollection permissionCollection) {
        Assert.hasText(roleId, "roleId must not be empty");
        this.roleId = roleId;
        this.permissionCollection = Objects.requireNonNull(permissionCollection, "permissionCollection must not be null");;
    }

    public RoleInformation() {
    }

    public void setPermissionCollection(PermissionCollection permissionCollection) {
        this.permissionCollection = permissionCollection;
    }

    public PermissionCollection getPermissionCollection() {
        return permissionCollection;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public static RoleInformation merge(RoleInformation roleInformation1, RoleInformation roleInformation2) {
        final String mergedUserId = roleInformation1.getRoleId() + "_" + roleInformation2.getRoleId();
        final PermissionCollection permissionCollection = PermissionCollection.merge(roleInformation1.getPermissionCollection(), roleInformation2.getPermissionCollection());
        return new RoleInformation(mergedUserId, permissionCollection);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoleInformation{");
        sb.append("roleName='").append(roleId).append('\'');
        sb.append(", permissionCollection=").append(permissionCollection);
        sb.append('}');
        return sb.toString();
    }
}
