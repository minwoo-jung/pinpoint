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
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class UserIdValidatorTest {

    @Test
    public void isValid() {
        UserIdValidator userIdValidator = new UserIdValidator();
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        assertFalse(userIdValidator.isValid("", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("uuu", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("1234567890123456789012345", constraintValidatorContext));
        assertTrue(userIdValidator.isValid("123456789012345678901234", constraintValidatorContext));

        //uppercase test
        assertFalse(userIdValidator.isValid("AAAaaa", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("aaaAAA", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("aAAAAa", constraintValidatorContext));

        //special character test
        assertFalse(userIdValidator.isValid("aaa.", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("a(aa", constraintValidatorContext));
        assertFalse(userIdValidator.isValid("!aaa", constraintValidatorContext));

        //success test
        assertTrue(userIdValidator.isValid("pinpoint-userid", constraintValidatorContext));
        assertTrue(userIdValidator.isValid("pinpoint-___", constraintValidatorContext));
        assertTrue(userIdValidator.isValid("pinpoint--_userid", constraintValidatorContext));
        assertTrue(userIdValidator.isValid("-pinpoint_userid-", constraintValidatorContext));
    }
}
