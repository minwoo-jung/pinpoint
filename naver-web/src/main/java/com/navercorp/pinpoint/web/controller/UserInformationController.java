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
import com.navercorp.pinpoint.web.util.AdditionValueValidator;
import com.navercorp.pinpoint.web.vo.*;
import com.navercorp.pinpoint.web.vo.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String ERROR_USER_PROFILE_INVALID = "User profile validation failed to creating user infomation.";
    private static final String ERROR_USER_EXITST = "User with (%s) that name already exists";
    private static final String ERROR_PASSWORD_INVALID = "User password validation failed.";
    private static final String ERROR_PASSWORD_INCORRECT = "The current password is incorrect.";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        final User userProfile = userInformation.getProfile();
        if (AdditionValueValidator.validateUser(userProfile) == false) {
            return ERROR_USER_PROFILE_INVALID;
        }
        if (userInformationService.isExistUserId(userProfile.getUserId())) {
            return String.format(ERROR_USER_EXITST, userProfile.getUserId());
        }
        final UserAccount userAccount  = userInformation.getAccount();
        if (AdditionValueValidator.validatePassword(userAccount.getPassword()) == false) {
            return ERROR_PASSWORD_INVALID;
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
        if(AdditionValueValidator.validateUser(user) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", ERROR_USER_PROFILE_INVALID);
            return result;
        }

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

        if (AdditionValueValidator.validatePassword(changedUserAccount.getNewPassword()) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", ERROR_PASSWORD_INVALID);
            logger.error(ERROR_PASSWORD_INVALID);
            return result;
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
    public Map<String, Object> selectPermissionAndConfiguration() {
        String userId = userService.getUserIdFromSecurity();

        RoleInformation roleInformation = roleService.getUserPermission(userId);

        List<ApplicationModel> favoriteApplicationList = userConfigService.selectFavoriteApplications(userId);
        Map<String, Object> userConfiguration = new HashMap<>();
        userConfiguration.put(UserConfigController.FAVORITE_APPLICATIONS, favoriteApplicationList);


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