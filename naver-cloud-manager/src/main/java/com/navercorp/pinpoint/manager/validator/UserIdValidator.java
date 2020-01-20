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

package com.navercorp.pinpoint.manager.validator;

import com.navercorp.pinpoint.manager.validator.constraint.UserIdConstraint;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HyunGil Jeong
 */
public class UserIdValidator extends ValueValidator implements ConstraintValidator<UserIdConstraint, String> {

    private static final int USER_ID_MAX_LENGTH = 24;
    private static final int USER_ID_MIN_LENGTH = 4;
    private static final String USER_ID_PATTERN_EXPRESSION = "[a-z0-9\\-_]+";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_PATTERN_EXPRESSION);

    @Override
    public void initialize(UserIdConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String userId, ConstraintValidatorContext constraintValidatorContext) {
        if (validateLength(userId, USER_ID_MAX_LENGTH, USER_ID_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = USER_ID_PATTERN.matcher(userId);
        return matcher.matches();
    }
}
