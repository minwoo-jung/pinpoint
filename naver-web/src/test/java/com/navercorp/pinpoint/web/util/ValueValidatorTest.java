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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ValueValidatorTest {

    @Test
    public void testValidateUserId() {
        //length test
        assertFalse(ValueValidator.validateUserId(""));
        assertFalse(ValueValidator.validateUserId("1234567890123456789012345"));
        assertTrue(ValueValidator.validateUserId("123456789012345678901234"));

        //uppercase test
        assertFalse(ValueValidator.validateUserId("AAAaaa"));
        assertFalse(ValueValidator.validateUserId("aaaAAA"));
        assertFalse(ValueValidator.validateUserId("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateUserId("aaa."));
        assertFalse(ValueValidator.validateUserId("a(aa"));
        assertFalse(ValueValidator.validateUserId("!aaa"));

        //success test
        assertTrue(ValueValidator.validateUserId("pinpoint-userid"));
        assertTrue(ValueValidator.validateUserId("pinpoint-___"));
        assertTrue(ValueValidator.validateUserId("pinpoint--_userid"));
        assertTrue(ValueValidator.validateUserId("-pinpoint_userid-"));
    }

    @Test
    public void testValidatePassword() {
        //length test
        assertFalse(ValueValidator.validatePassword(""));
        assertFalse(ValueValidator.validatePassword("12345678901234567890123456789012"));
        assertTrue(ValueValidator.validatePassword("AAAAbbbb1234!@#"));

        //whitespace  character
        assertFalse(ValueValidator.validatePassword("aaa   aa"));
        assertFalse(ValueValidator.validatePassword("   aaa"));
        assertFalse(ValueValidator.validatePassword("aaa   "));

        //requirement
        assertFalse(ValueValidator.validatePassword("1234!@#"));
        assertFalse(ValueValidator.validatePassword("AAAAbbbb!@#"));
        assertFalse(ValueValidator.validatePassword("AAAAbbbb1234"));
        assertFalse(ValueValidator.validatePassword("bbbb1234!@#가나다"));
        assertTrue(ValueValidator.validatePassword("bbbb1234!@#"));
        assertTrue(ValueValidator.validatePassword("AAAA1234!@#"));
        assertTrue(ValueValidator.validatePassword("1234!@#bbb"));
    }

    @Test
    public void testValidateOrganization() {
        //length test
        assertFalse(ValueValidator.validateOrganization(""));
        assertFalse(ValueValidator.validateOrganization("12345678901234567890123456789012345678901"));
        assertTrue(ValueValidator.validateOrganization("1234567890123456789012345678901234567890"));

        //uppercase test
        assertTrue(ValueValidator.validateOrganization("AAAaaa"));
        assertTrue(ValueValidator.validateOrganization("aaaAAA"));
        assertTrue(ValueValidator.validateOrganization("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateOrganization("aaa."));
        assertFalse(ValueValidator.validateOrganization("a(aa"));
        assertFalse(ValueValidator.validateOrganization("!aaa"));

        //success test
        assertTrue(ValueValidator.validateOrganization("pinpoint-naver"));
        assertTrue(ValueValidator.validateOrganization("pinpoint-___"));
        assertTrue(ValueValidator.validateOrganization("pinpoint--_naver"));
        assertTrue(ValueValidator.validateOrganization("-pinpoint_naver-"));
    }

    @Test
    public void testValidateName() {
        //length test
        assertFalse(ValueValidator.validateName(""));
        assertFalse(ValueValidator.validateName("1234567890123456789012345678901234"));
        assertTrue(ValueValidator.validateName("123456789012345678901234567890"));

        //uppercase test
        assertTrue(ValueValidator.validateName("AAAaaa"));
        assertTrue(ValueValidator.validateName("aaaAAA"));
        assertTrue(ValueValidator.validateName("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateName("aaa!@#"));
        assertFalse(ValueValidator.validateName("a(aa"));
        assertFalse(ValueValidator.validateName("!aaa"));

        //success test
        assertTrue(ValueValidator.validateName("pinpoint-name"));
        assertTrue(ValueValidator.validateName("pinpoint-___"));
        assertTrue(ValueValidator.validateName("pinpoint--_name"));
        assertTrue(ValueValidator.validateName("-pinpoint_name-"));
        assertTrue(ValueValidator.validateName(".pinpoint.name."));
    }

    @Test
    public void testValidateRoleId() {
        //length test
        assertFalse(ValueValidator.validateRoleId(""));
        assertFalse(ValueValidator.validateRoleId("1234567890123456789012345"));
        assertTrue(ValueValidator.validateRoleId("123456789012345678901234"));

        //uppercase test
        assertTrue(ValueValidator.validateRoleId("AAAaaa"));
        assertTrue(ValueValidator.validateRoleId("aaaAAA"));
        assertTrue(ValueValidator.validateRoleId("aAAAAa"));

        //special character test
        assertFalse(ValueValidator.validateRoleId("aaa!@#"));
        assertFalse(ValueValidator.validateRoleId("a(aa"));
        assertFalse(ValueValidator.validateRoleId("!aaa"));
        assertFalse(ValueValidator.validateRoleId(".aaa"));

        //success test
        assertTrue(ValueValidator.validateRoleId("role-id"));
        assertTrue(ValueValidator.validateRoleId("roleid-___"));
        assertTrue(ValueValidator.validateRoleId("role--_id"));
        assertTrue(ValueValidator.validateRoleId("-role_id-"));
    }

    @Test
    public void testValidatePhonenumber() {
        //length test
        assertFalse(ValueValidator.validatePhonenumber(""));
        assertFalse(ValueValidator.validatePhonenumber("1234567890123456789012345"));
        assertTrue(ValueValidator.validatePhonenumber("123456789012345678901234"));

        //character test
        assertFalse(ValueValidator.validatePhonenumber("AAA"));
        assertFalse(ValueValidator.validatePhonenumber("aaa"));
        assertFalse(ValueValidator.validatePhonenumber("!#$"));
        assertFalse(ValueValidator.validatePhonenumber("123AABB"));
        assertFalse(ValueValidator.validatePhonenumber("123!@bb"));

        //success test
        assertTrue(ValueValidator.validatePhonenumber("123455667"));
    }

    @Test
    public void testValidateEmail() {
        //length test
        assertFalse(ValueValidator.validateEmail(""));
        assertFalse(ValueValidator.validateEmail("1234567890123456789012345678901234567890123456789012345678901"));
        assertTrue(ValueValidator.validateEmail("12345678901234567@naver.com"));

        //character test
        assertFalse(ValueValidator.validateEmail("AAA!@#@naver.com"));
        assertFalse(ValueValidator.validateEmail("AAA.com"));
        assertFalse(ValueValidator.validateEmail("@@@@naver.com"));
        assertFalse(ValueValidator.validateEmail("pinpoint_dev@navercorp.!@#"));
        assertFalse(ValueValidator.validateEmail("pinpoint_dev@naver!!corp.com"));
        assertFalse(ValueValidator.validateEmail("pinpoint##dev@navercorp.com"));

        //success test
        assertTrue(ValueValidator.validateEmail("pinpoint_dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint-dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@navercorp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@naver.corp.com"));
        assertTrue(ValueValidator.validateEmail("pinpoint.dev@naver-corp.com"));
    }

}