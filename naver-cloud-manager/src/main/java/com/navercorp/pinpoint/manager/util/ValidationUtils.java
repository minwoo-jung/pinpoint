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

package com.navercorp.pinpoint.manager.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * @author HyunGil Jeong
 */
public class ValidationUtils {

    private static final String VALID_ORGANIZATION_NAME_REGEX = "(?:[a-zA-Z_0-9]+)";
    private static final Pattern VALID_ORGANIZATION_PATTERN = Pattern.compile(VALID_ORGANIZATION_NAME_REGEX);

    public static boolean isValidOrganizationName(String organizationName) {
        if (StringUtils.isEmpty(organizationName)) {
            return false;
        }
        return VALID_ORGANIZATION_PATTERN.matcher(organizationName).matches();
    }
}
