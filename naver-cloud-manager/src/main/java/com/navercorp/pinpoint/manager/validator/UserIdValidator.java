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

/**
 * @author HyunGil Jeong
 */
public class UserIdValidator implements ConstraintValidator<UserIdConstraint, String> {

    @Override
    public void initialize(UserIdConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String userId, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        // TODO implement user id validation
        return true;
    }
}
