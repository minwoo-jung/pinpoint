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

package com.navercorp.pinpoint.manager.domain.mysql.repository.role;

/**
 * Copied from com.navercorp.pinpoint.web.vo.role.PermsGroupAdministration
 *
 * @author HyunGil Jeong
 */
// TODO refactor web commons
public class PermsGroupAdministration {

    public static final PermsGroupAdministration ADMIN = new PermsGroupAdministration(true, true, true, true);
    public static final PermsGroupAdministration DEFAULT = new PermsGroupAdministration(false, false, false, false);
    public static final String ADMINISTRATION = "administration";
    public static final String VIEW_ADMIN_MENU = "viewAdminMenu";
    public static final String EDIT_USER = "editUser";
    public static final String EDIT_ROLE = "editRole";
    public static final String CALL_API_FOR_APP_AGENT_MANAGEMENT = "callApiForAppAgentManagement";

    private boolean viewAdminMenu;

    private boolean editUser; //userInformation insert, update, delete permission

    private boolean editRole;

    private boolean callAdminApi;

    public PermsGroupAdministration(boolean viewAdminMenu, boolean editUser, boolean editRole, boolean callAdminApi) {
        this.viewAdminMenu = viewAdminMenu;
        this.editUser = editUser;
        this.editRole = editRole;
        this.callAdminApi = callAdminApi;
    }

    public PermsGroupAdministration() {
    }

    public boolean getEditRole() {
        return editRole;
    }

    public boolean getViewAdminMenu() {
        return viewAdminMenu;
    }

    public void setEditRole(boolean editRole) {
        this.editRole = editRole;
    }

    public boolean getEditUser() {
        return editUser;
    }

    public void setViewAdminMenu(boolean viewAdminMenu) {
        this.viewAdminMenu = viewAdminMenu;
    }

    public void setEditUser(boolean editUser) {
        this.editUser = editUser;
    }

    public boolean getCallAdminApi() {
        return callAdminApi;
    }

    public void setCallAdminApi(boolean callAdminApi) {
        this.callAdminApi = callAdminApi;
    }

    public static PermsGroupAdministration merge(PermsGroupAdministration permsGroupAdministration1, PermsGroupAdministration permsGroupAdministration2) {
        final boolean viewAdminMenu = permsGroupAdministration1.getViewAdminMenu() || permsGroupAdministration2.getViewAdminMenu();
        final boolean editUser = permsGroupAdministration1.getEditUser() || permsGroupAdministration2.getEditUser();
        final boolean editRole = permsGroupAdministration1.getEditRole() || permsGroupAdministration2.getEditRole();
        final boolean callAdminApi = permsGroupAdministration1.getCallAdminApi() || permsGroupAdministration2.getCallAdminApi();
        return new PermsGroupAdministration(viewAdminMenu, editUser, editRole, callAdminApi);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupAdministration{");
        sb.append("viewAdminMenu=").append(viewAdminMenu);
        sb.append(", editUser=").append(editUser);
        sb.append(", editRole=").append(editRole);
        sb.append(", callAdminApi=").append(callAdminApi);
        sb.append('}');
        return sb.toString();
    }
}
