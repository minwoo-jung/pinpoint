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

import com.navercorp.pinpoint.manager.validator.constraint.OrganizationNameConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HyunGil Jeong
 */
public class OrganizationNameValidator extends ValueValidator implements ConstraintValidator<OrganizationNameConstraint, String> {

    private static final int ORGANIZATION_MAX_LENGTH = 20;
    private static final int ORGANIZATION_MIN_LENGTH = 3;
    private static final String ORGANIZATION_PATTERN_EXPRESSION = "[A-Za-z0-9\\-_]+";
    private static final Pattern ORGANIZATION_PATTERN = Pattern.compile(ORGANIZATION_PATTERN_EXPRESSION);

    @Override
    public void initialize(OrganizationNameConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String organizationName, ConstraintValidatorContext constraintValidatorContext) {
        if (validateLength(organizationName, ORGANIZATION_MAX_LENGTH, ORGANIZATION_MIN_LENGTH) == false) {
            return false;
        }

        final Matcher matcher = ORGANIZATION_PATTERN.matcher(organizationName);
        return matcher.matches();
    }
}
