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
package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author minwoo.jung
 */
public class ValueValidator {

    private static final int USER_ID_MAX_LENGTH = 24;
    private static final String USER_ID_PATTERN_EXPRESSION = "[a-z0-9\\-_]+";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_PATTERN_EXPRESSION);

    private static final int PASSWORD_MAX_LENGTH = 30;
    private static final String PASSWORD_PATTERN_EXPRESSION = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%\\^&*\\(\\)])[A-Za-z\\d!@#$%\\^&*\\(\\)]+$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_EXPRESSION);


    private static final int ORGANIZATION_MAX_LENGTH = 40;
    private static final String ORGANIZATION_PATTERN_EXPRESSION = "[A-Za-z0-9\\-_]+";
    private static final Pattern ORGANIZATION_PATTERN = Pattern.compile(ORGANIZATION_PATTERN_EXPRESSION);

    private static final int NAME_MAX_LENGTH = 30;
    private static final String NAME_PATTERN_EXPRESSION = "[A-Za-z0-9\\.\\-_]+";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_EXPRESSION);

    private static final int ROLE_ID_MAX_LENGTH = 24;
    private static final String ROLE_ID_PATTERN_EXPRESSION = "[A-Za-z0-9\\-_]+";
    private static final Pattern ROLE_ID_PATTERN = Pattern.compile(ROLE_ID_PATTERN_EXPRESSION);

    private static final int PHONENUMBER_MAX_LENGTH = 24;
    private static final String PHONENUMBER_PATTERN_EXPRESSION = "[0-9]+";
    private static final Pattern PHONENUMBER_PATTERN = Pattern.compile(PHONENUMBER_PATTERN_EXPRESSION);

    private static final int EMAIL_MAX_LENGTH = 60;
    private static final String EMAIL_PATTERN_EXPRESSION = "^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_PATTERN_EXPRESSION);

    public static boolean validateUserId(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = USER_ID_PATTERN.matcher(userId);
        return matcher.matches();
    }

    public static boolean validatePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return false;
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = PASSWORD_PATTERN.matcher(password);
        matcher.matches();
        return matcher.matches();
    }

    public static boolean validateOrganization(String organization) {
        if (StringUtils.isEmpty(organization)) {
            return false;
        }
        if (organization.length() > ORGANIZATION_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = ORGANIZATION_PATTERN.matcher(organization);
        return matcher.matches();
    }

    public static boolean validateName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        if (name.length() > NAME_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    public static boolean validateRoleId(String roleId) {
        if (StringUtils.isEmpty(roleId)) {
            return false;
        }
        if (roleId.length() > ROLE_ID_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = ROLE_ID_PATTERN.matcher(roleId);
        return matcher.matches();
    }

    public static boolean validatePhonenumber(String phonenumber) {
        if (StringUtils.isEmpty(phonenumber)) {
            return false;
        }
        if (phonenumber.length() > PHONENUMBER_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = PHONENUMBER_PATTERN.matcher(phonenumber);
        return matcher.matches();
    }

    public static boolean validateEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}
