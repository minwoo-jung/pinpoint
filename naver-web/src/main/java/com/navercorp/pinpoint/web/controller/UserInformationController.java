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
package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.security.NaverPermissionEvaluator;
import com.navercorp.pinpoint.web.security.PermissionChecker;
import com.navercorp.pinpoint.web.service.*;
import com.navercorp.pinpoint.web.vo.*;
import com.navercorp.pinpoint.web.vo.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */

@Controller
@RequestMapping(value= "users")
public class UserInformationController {
    private static final String EMPTY = "";
    private static final String ERROR_USER_ID_NULL = "you must enter a user id.";
    private static final String ERROR_USER_EXITST = "User with (%s) that name already exists";
    private static final String ERROR_PASSWORD_NULL = "You must enter a password.";
    private static final String ERROR_NAME_NULL = "You must enter a name.";
    private static final String ERROR_PASSWORD_INCORRECT = "The current password is incorrect.";
    private static final String SSO_USER = "SSO_USER";
    private static final RoleInformation FIXED_ROLE;

    static {
        PermsGroupAdministration permsGroupAdministration = new PermsGroupAdministration();
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(true, false, true);
        PermsGroupAlarm permsGroupAlarm = new PermsGroupAlarm(false, true);
        PermsGroupUserGroup permsGroupUserGroup = new PermsGroupUserGroup(false, true);
        PermissionCollection permissionCollection = new PermissionCollection(permsGroupAdministration, permsGroupAppAuthorization, permsGroupAlarm, permsGroupUserGroup);
        FIXED_ROLE = new RoleInformation("fixedRole", permissionCollection);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{pinpointWebProps['user.permission.use.fixed.value'] ?: true}")
    private boolean isAlwaysDefaultPermission;

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private NaverPermissionEvaluator naverPermissionEvaluator;

    //TODO : (minwoo) 파라미터 validation 체크 필요함.
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getUsers(@RequestParam(value="userId", required=false) String userId, @RequestParam(value="searchKey", required=false) String searchKey) {
        try {
            if(userId != null) {
                List<User> users = new ArrayList<>(1);
                users.add(userService.selectUserByUserId(userId));
                return users;
            } else if (searchKey != null) {
                List<User> users = userService.selectUserByDepartment(searchKey);
                users.addAll(userService.selectUserByUserName(searchKey));
                return users;
            } else {
                return userService.selectUser();
            }
        } catch (Exception e) {
            logger.error("can't select user", e);

            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "This api need to collect condition for search.");
            return result;
        }
    }

    @RequestMapping(value="user", method = RequestMethod.GET)
    @ResponseBody
    public Object selectUserInformation(@RequestParam(value="userId") String userId) {
        try {
            return userInformationService.selectUserInformation(userId);
        } catch (Exception e) {
            logger.error("can't select user", e);

            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "userId(" + userId + ") doesn't exist.");
            return result;
        }
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_USER)")
    @RequestMapping(value="user", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserInformation(@RequestBody UserInformation userInformation) {
        String message = checkUserInformation(userInformation);
        if (StringUtils.hasLength(message)) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", message);
            logger.error(message);
            return result;
        }

        String userId = userInformation.getProfile().getUserId();
        userInformation.getAccount().setUserId(userId);
        userInformation.getRole().setUserId(userId);
        userInformationService.insertUserInformation(userInformation);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    private String checkUserInformation(UserInformation userInformation) {
        User userProfile = userInformation.getProfile();
        if (StringUtils.isEmpty(userProfile.getUserId())) {
            return ERROR_USER_ID_NULL;
        }
        if (userInformationService.isExistUserId(userProfile.getUserId())) {
            return String.format(ERROR_USER_EXITST, userProfile.getUserId());
        }
        if (StringUtils.isEmpty(userProfile.getName())) {
            return ERROR_NAME_NULL;
        }

        UserAccount userAccount  = userInformation.getAccount();
        if (StringUtils.isEmpty(userAccount.getPassword())) {
            return ERROR_PASSWORD_NULL;
        }

        return EMPTY;
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_USER)")
    @RequestMapping(value="user", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserInformation(@RequestBody User user) {
        userInformationService.deleteUserInformation(user.getUserId());

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }


    @PreAuthorize("hasPermission(#user.getUserId(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_USER)")
    @RequestMapping(value="user/profile", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserProfile(@RequestBody User user) {
        userService.updateUser(user);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @PreAuthorize("hasPermission(#changedUserAccount.getUserId(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_USER)")
    @RequestMapping(value="user/account", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserProfile(@RequestBody ChangedUserAccount changedUserAccount) {

        if (naverPermissionEvaluator.hasPermission(PermissionChecker.PERMISSION_ADMINISTRATION_EDIT_USER) == false) {
            boolean isCollectPassword = userAccountService.isCollectPassword(new UserAccount(changedUserAccount.getUserId(), changedUserAccount.getCurrentPassword()));

            if (isCollectPassword == false) {
                Map<String, String> result = new HashMap<>();
                result.put("errorCode", "500");
                result.put("errorMessage", ERROR_PASSWORD_INCORRECT);
                logger.error(ERROR_PASSWORD_INCORRECT);
                return result;
            }
        }

        userAccountService.updateUserAccount(new UserAccount(changedUserAccount.getUserId(), changedUserAccount.getNewPassword()));

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_USER)")
    @RequestMapping(value="user/role", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserRole(@RequestBody UserRole userRole) {
        roleService.updateUserRole(userRole);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(value="user/permissionAndConfiguration", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> selectPermissionAndConfiguration(@RequestHeader(SSO_USER) String userId) {
        UserConfiguration userConfiguration = userConfigService.selectUserConfiguration(userId);

        RoleInformation roleInformation = FIXED_ROLE;
        if (isAlwaysDefaultPermission == false) {
            roleInformation = roleService.getUserPermission(userId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("configuration", userConfiguration);
        result.put("permission", roleInformation);
        return result;
    }

    public static class ChangedUserAccount {

        private String userId;
        private String currentPassword;
        private String newPassword;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

}