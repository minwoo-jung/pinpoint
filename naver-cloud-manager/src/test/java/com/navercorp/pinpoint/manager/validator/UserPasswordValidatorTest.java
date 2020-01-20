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
public class UserPasswordValidatorTest {

    @Test
    public void isValid() {
        UserPasswordValidator userPasswordValidator = new UserPasswordValidator();
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        //length test
        assertFalse(userPasswordValidator.isValid("", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("1234567", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("12345678901234567890123456789012", constraintValidatorContext));
        assertTrue(userPasswordValidator.isValid("AAAAbbbb1234!@#", constraintValidatorContext));

        //whitespace  character
        assertFalse(userPasswordValidator.isValid("aaa   aa", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("   aaa", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("aaa   ", constraintValidatorContext));

        //requirement
        assertFalse(userPasswordValidator.isValid("1234!@#", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("AAAAbbbb!@#", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("AAAAbbbb1234", constraintValidatorContext));
        assertFalse(userPasswordValidator.isValid("bbbb1234!@#가나다", constraintValidatorContext));
        assertTrue(userPasswordValidator.isValid("bbbb1234!@#", constraintValidatorContext));
        assertTrue(userPasswordValidator.isValid("AAAA1234!@#", constraintValidatorContext));
        assertTrue(userPasswordValidator.isValid("1234!@#bbb", constraintValidatorContext));
    }
}