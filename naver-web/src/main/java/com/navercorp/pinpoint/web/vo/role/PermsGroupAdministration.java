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

/**
 * @author minwoo.jung
 */
public class PermsGroupAdministration {

    public final static PermsGroupAdministration DEFAULT = new PermsGroupAdministration(false, false, false, false);
    public final static String ADMINISTRATION = "administration";
    public final static String VIEW_ADMIN_MENU = "viewAdminMenu";
    public final static String EDIT_USER = "editUser";
    public final static String EDIT_ROLE = "editRole";
    public final static String CALL_API_FOR_APP_AGENT_MANAGEMENT = "callApiForAppAgentManagement";

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