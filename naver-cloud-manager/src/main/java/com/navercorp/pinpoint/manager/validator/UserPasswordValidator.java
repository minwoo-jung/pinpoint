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

import com.navercorp.pinpoint.manager.validator.constraint.UserPasswordConstraint;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HyunGil Jeong
 */
public class UserPasswordValidator extends ValueValidator implements ConstraintValidator<UserPasswordConstraint, String> {

    private static final int PASSWORD_MAX_LENGTH = 30;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final String PASSWORD_PATTERN_EXPRESSION = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%\\^&*\\(\\)])[A-Za-z\\d!@#$%\\^&*\\(\\)]+$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_EXPRESSION);

    @Override
    public void initialize(UserPasswordConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (validateLength(password, PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }
}
