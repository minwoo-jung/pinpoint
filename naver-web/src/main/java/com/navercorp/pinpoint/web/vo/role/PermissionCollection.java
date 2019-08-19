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

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class PermissionCollection {

    public final static String PERMISSION = "permission";
    public final static PermissionCollection DEFAULT = new PermissionCollection(PermsGroupAdministration.DEFAULT, PermsGroupAppAuthorization.DEFAULT, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT);

    private PermsGroupAdministration permsGroupAdministration;
    private PermsGroupAppAuthorization permsGroupAppAuthorization;
    private PermsGroupAlarm permsGroupAlarm;
    private PermsGroupUserGroup permsGroupUserGroup;


    public PermissionCollection() {
    }

    public PermissionCollection(PermsGroupAdministration permsGroupAdministration, PermsGroupAppAuthorization permsGroupAppAuthorization, PermsGroupAlarm permsGroupAlarm, PermsGroupUserGroup permsGroupUserGroup) {
        this.permsGroupAdministration = Objects.requireNonNull(permsGroupAdministration, "permsGroupAdministration must not be null");;
        this.permsGroupAppAuthorization = Objects.requireNonNull(permsGroupAppAuthorization, "permsGroupAppAuthorization must not be null");;
        this.permsGroupAlarm = Objects.requireNonNull(permsGroupAlarm, "permsGroupAlarm must not be null");;
        this.permsGroupUserGroup = Objects.requireNonNull(permsGroupUserGroup, "permsGroupUserGroup must not be null");;
    }

    public static PermissionCollection merge(PermissionCollection permissionCollection1, PermissionCollection permissionCollection2) {
        final PermsGroupAdministration mergedPermsGroupAdministration = PermsGroupAdministration.merge(permissionCollection1.getPermsGroupAdministration(), permissionCollection2.getPermsGroupAdministration());
        final PermsGroupAppAuthorization mergedPermsGroupAppAuthorization = PermsGroupAppAuthorization.merge(permissionCollection1.getPermsGroupAppAuthorization(), permissionCollection2.getPermsGroupAppAuthorization());
        final PermsGroupAlarm mergedPermsGroupAlarm = PermsGroupAlarm.merge(permissionCollection1.getPermsGroupAlarm(), permissionCollection2.getPermsGroupAlarm());
        final PermsGroupUserGroup mergedPermsGroupUserGroup = PermsGroupUserGroup.merge(permissionCollection1.getPermsGroupUserGroup(), permissionCollection2.getPermsGroupUserGroup());
        return new PermissionCollection(mergedPermsGroupAdministration, mergedPermsGroupAppAuthorization, mergedPermsGroupAlarm, mergedPermsGroupUserGroup);
    }

    public PermsGroupAdministration getPermsGroupAdministration() {
        return permsGroupAdministration;
    }

    public PermsGroupAppAuthorization getPermsGroupAppAuthorization() {
        return permsGroupAppAuthorization;
    }

    public PermsGroupAlarm getPermsGroupAlarm() {
        return permsGroupAlarm;
    }

    public PermsGroupUserGroup getPermsGroupUserGroup() {
        return permsGroupUserGroup;
    }

    public void setPermsGroupAlarm(PermsGroupAlarm permsGroupAlarm) {
        this.permsGroupAlarm = permsGroupAlarm;
    }

    public void setPermsGroupUserGroup(PermsGroupUserGroup permsGroupUserGroup) {
        this.permsGroupUserGroup = permsGroupUserGroup;
    }

    public void setPermsGroupAdministration(PermsGroupAdministration permsGroupAdministration) {
        this.permsGroupAdministration = permsGroupAdministration;
    }

    public void setPermsGroupAppAuthorization(PermsGroupAppAuthorization permsGroupAppAuthorization) {
        this.permsGroupAppAuthorization = permsGroupAppAuthorization;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermissionCollection{");
        sb.append("permsGroupAdministration=").append(permsGroupAdministration);
        sb.append(", permsGroupAppAuthorization=").append(permsGroupAppAuthorization);
        sb.append(", permsGroupAlarm=").append(permsGroupAlarm);
        sb.append(", permsGroupUserGroup=").append(permsGroupUserGroup);
        sb.append('}');
        return sb.toString();
    }
}
