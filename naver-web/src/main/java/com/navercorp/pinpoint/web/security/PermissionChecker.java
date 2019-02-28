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

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.role.*;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author minwoo.jung
 */
public final class PermissionChecker extends PermissionCollectionDefinition {

    private static final String EMPTY_STRING = "";


    @Autowired
    ApplicationConfigService applicationConfigService;

    @Autowired
    UserGroupService userGroupService;


    public boolean checkPermission(PinpointAuthentication pinAuth, String permissionCollection, Object parameter) {
        final String permissionCollectionCategory = extractPermissionCollectionCategory(permissionCollection);

        final String permissionName = extractPermission(permissionCollectionCategory);
        final String subPermissionCategory = extractSubPermissions(permissionCollectionCategory);
        final PermissionCollection permCollection = pinAuth.getRoleInformation().getPermissionCollection();

        if (PermsGroupAdministration.ADMINISTRATION.equals(permissionName)) {
            return checkPermsGroupAdministration(permCollection.getPermsGroupAdministration(), subPermissionCategory, parameter, pinAuth.getPrincipal());
        } else if (PermsGroupAppAuthorization.APP_AUTHORIZATION.equals(permissionName)) {
            return checkPermsGroupAppAuthorization(permCollection.getPermsGroupAppAuthorization(), subPermissionCategory, parameter, pinAuth.getPrincipal());
        } else if (PermsGroupAlarm.ALARM.equals(permissionName)) {
            return checkPermsGroupAlarm(permCollection.getPermsGroupAlarm(), subPermissionCategory, parameter, pinAuth.getPrincipal());
        } else if (PermsGroupUserGroup.USER_GROUP.equals(permissionName)) {
            return checkPermsGroupUserGroup(permCollection.getPermsGroupUserGroup(), subPermissionCategory, parameter, pinAuth.getPrincipal());
        } else {
            return false;
        }
    }

    private boolean checkPermsGroupUserGroup(PermsGroupUserGroup permsGroupUserGroup, String permission, Object parameter, String userId) {
        if (PermsGroupUserGroup.EDIT_GROUP_FOR_EVERYTHING.equals(permission)) {
            return permsGroupUserGroup.getEditGroupForEverything();
        } else if (PermsGroupUserGroup.EDIT_GROUP_ONLY_GROUPMEMBER.equals(permission)) {
            if (permsGroupUserGroup.getEditGroupForEverything()) {
                return true;
            }
            if (permsGroupUserGroup.getEditGroupOnlyGroupMember()) {
                if (parameter instanceof String) {
                    return userGroupService.checkValid(userId, (String)parameter);
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean checkPermsGroupAlarm(PermsGroupAlarm permsGroupAlarm, String permission, Object parameter,String userId) {
        if (PermsGroupAlarm.EDIT_ALARM_FOR_EVERYTHING.equals(permission)) {
            return permsGroupAlarm.getEditAlarmForEverything();
        } else if (PermsGroupAlarm.EDIT_ALARM_ONLY_GROUPMEMBER.equals(permission)) {
            if (permsGroupAlarm.getEditAlarmForEverything()) {
                return true;
            }
            if (permsGroupAlarm.getEditAlarmOnlyGroupMember()) {
                if (parameter instanceof String) {
                    return applicationConfigService.canEditConfiguration((String) parameter, userId);
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean checkPermsGroupAppAuthorization(PermsGroupAppAuthorization permsGroupAppAuthorization, String permission, Object parameter, String userId) {
        if (PermsGroupAppAuthorization.PREOCCUPANCY.equals(permission)) {
            return permsGroupAppAuthorization.getPreoccupancy();
        } else if (PermsGroupAppAuthorization.EDIT_AUTHOR_FOR_EVERYTHING.equals(permission)) {
            return permsGroupAppAuthorization.getEditAuthorForEverything();
        } else if (PermsGroupAppAuthorization.EDIT_AUTHOR_ONLY_MANAGER.equals(permission)) {
            if (permsGroupAppAuthorization.getEditAuthorForEverything()) {
                return true;
            }
            if (permsGroupAppAuthorization.getEditAuthorOnlyManager()) {
                if (parameter instanceof String) {
                    return applicationConfigService.canEditConfiguration((String) parameter, userId);
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static boolean checkPermsGroupAdministration(PermsGroupAdministration permsGroupAdministration, String permission, Object parameter, String userId) {
        if (PermsGroupAdministration.VIEW_ADMIN_MENU.equals(permission)) {
            return permsGroupAdministration.getViewAdminMenu();
        } else if (PermsGroupAdministration.EDIT_USER.equals(permission)) {
            if (permsGroupAdministration.getEditUser()) {
                return true;
            }
            if ((parameter instanceof String) == false) {
                return false;
            }
            if (userId.equals(parameter)) {
                return true;
            }

            return false;
        } else if (PermsGroupAdministration.EDIT_ROLE.equals(permission)) {
            return permsGroupAdministration.getEditRole();
        } else {
            return false;
        }
    }

    public static String extractPermissionCollectionCategory(String permissionCollection) {
        return permissionCollection.replaceFirst(PREFIX_PERMISSION, EMPTY_STRING);
    }

    private static String extractPermission(String permission) {
        int delimiterIndex = permission.indexOf(DELIMITER);

        if (delimiterIndex == -1) {
            return EMPTY_STRING;
        }

        return permission.substring(0, delimiterIndex);
    }

    private static String extractSubPermissions(String permission) {
        int delimiterIndex = permission.indexOf(DELIMITER);

        if (delimiterIndex == -1) {
            return EMPTY_STRING;
        }

        return permission.substring(delimiterIndex + 1, permission.length());
    }
}
