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
package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.web.controller.AdminController;
import com.navercorp.pinpoint.web.controller.AlarmController;
import com.navercorp.pinpoint.web.controller.UserGroupController;
import com.navercorp.pinpoint.web.vo.role.PermissionCollection;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAdministration;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAlarm;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAppAuthorization;
import com.navercorp.pinpoint.web.vo.role.PermsGroupUserGroup;

/**
 * @author minwoo.jung
 */
public class PermissionCollectionDefinition {

    public static final String DELIMITER = "_";

    //permission_
    public static final String PREFIX_PERMISSION = PermissionCollection.PERMISSION + DELIMITER;

    //permission_administration
    private static final String PREFIX_ADMINISTRATION = PREFIX_PERMISSION + PermsGroupAdministration.ADMINISTRATION + DELIMITER;
    //permission_administration_viewAdminMenu
    public static final String PERMISSION_ADMINISTRATION_VIEW_ADMIN_MENU = PREFIX_ADMINISTRATION + PermsGroupAdministration.VIEW_ADMIN_MENU;
    //permission_administration_editUser
    public static final String PERMISSION_ADMINISTRATION_EDIT_USER = PREFIX_ADMINISTRATION + PermsGroupAdministration.EDIT_USER;
    //permission_administration_editRole
    public static final String PERMISSION_ADMINISTRATION_EDIT_ROLE = PREFIX_ADMINISTRATION + PermsGroupAdministration.EDIT_ROLE;
    //permission_administration_callAdminApi
    public static final String PERMISSION_ADMINISTRATION_CALL_API_FOR_APP_AGENT_MANAGEMENT = AdminController.CALL_API_FOR_APP_AGENT_MANAGEMENT;

    //permission_appAuthorization
    private static final String PREFIX_APP_AUTHORIZATION = PREFIX_PERMISSION + PermsGroupAppAuthorization.APP_AUTHORIZATION + DELIMITER;
    //permission_appAuthorization_preoccupancy
    public static final String PERMISSION_APPAUTHORIZATION_PREOCCUPANCY = PREFIX_APP_AUTHORIZATION + PermsGroupAppAuthorization.PREOCCUPANCY;
    //permission_appAuthorization_editAuthorForEverything
    public static final String PERMISSION_APPAUTHORIZATION_EDIT_AUTHOR_FOR_EVERYTHING = PREFIX_APP_AUTHORIZATION + PermsGroupAppAuthorization.EDIT_AUTHOR_FOR_EVERYTHING;
    //permission_appAuthorization_editAuthorOnlyManager
    public static final String PERMISSION_APPAUTHORIZATION_EDIT_AUTHOR_ONLY_MANAGER = PREFIX_APP_AUTHORIZATION + PermsGroupAppAuthorization.EDIT_AUTHOR_ONLY_MANAGER;
    //permission_appAuthorization_obtainAllAuthorization
    public static final String PERMISSION_APPAUTHORIZATION_OBTAIN_ALL_AUTHORIZATION = PREFIX_APP_AUTHORIZATION + PermsGroupAppAuthorization.OBTAIN_ALL_AUTHORIZATION;

    //permission_alarm_
    private static final String PREFIX_ALARM = PREFIX_PERMISSION + PermsGroupAlarm.ALARM + DELIMITER;
    //permission_alarm_editAlarmForEverything
    public static final String PERMISSION_ALARM_EDIT_ALARM_FOR_EVERYTHING = PREFIX_ALARM + PermsGroupAlarm.EDIT_ALARM_FOR_EVERYTHING;
    //permission_alarm_editAlarmOnlyGroupMember
    public static final String PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER = AlarmController.EDIT_ALARM_ONLY_MANAGER;

    //permission_userGroup_
    private static final String PREFIX_USERGROUP = PREFIX_PERMISSION + PermsGroupUserGroup.USER_GROUP + DELIMITER;
    //permission_userGroup_editGroupForEverything
    public static final String PERMISSION_USERGROUP_EDIT_GROUP_FOR_EVERYTHING = PREFIX_USERGROUP + PermsGroupUserGroup.EDIT_GROUP_FOR_EVERYTHING;
    //permission_userGroup_editGroupOnlyGroupMember
    public static final String PERMISSION_USERGROUP_EDIT_GROUP_ONLY_GROUPMEMBER = UserGroupController.EDIT_GROUP_ONLY_GROUPMEMBER;
}
