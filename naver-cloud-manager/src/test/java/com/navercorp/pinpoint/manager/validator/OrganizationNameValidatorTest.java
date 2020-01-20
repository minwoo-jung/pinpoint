/*
 * Copyright 2020 NAVER Corp.
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

import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class OrganizationNameValidatorTest {

    @Test
    public void isValid() {

        OrganizationNameValidator organizationNameValidator = new OrganizationNameValidator();
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

        //length test
        assertFalse(organizationNameValidator.isValid("", constraintValidatorContext));
        assertFalse(organizationNameValidator.isValid("or", constraintValidatorContext));
        assertFalse(organizationNameValidator.isValid("123456789012345678901", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("12345678901234567890", constraintValidatorContext));

        //uppercase test
        assertTrue(organizationNameValidator.isValid("AAAaaa", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("aaaAAA", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("aAAAAa", constraintValidatorContext));

        //special character test
        assertFalse(organizationNameValidator.isValid("aaa.", constraintValidatorContext));
        assertFalse(organizationNameValidator.isValid("a(aa", constraintValidatorContext));
        assertFalse(organizationNameValidator.isValid("!aaa", constraintValidatorContext));

        //success test
        assertTrue(organizationNameValidator.isValid("pinpoint-naver", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("pinpoint-___", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("pinpoint--_naver", constraintValidatorContext));
        assertTrue(organizationNameValidator.isValid("-pinpoint_naver-", constraintValidatorContext));
    }
}