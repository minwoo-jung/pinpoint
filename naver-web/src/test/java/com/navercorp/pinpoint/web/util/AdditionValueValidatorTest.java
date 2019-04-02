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
public class AdditionValueValidatorTest {
    @Test
    public void testValidateOrganization() {
        //length test
        assertFalse(AdditionValueValidator.validateOrganization(""));
        assertFalse(AdditionValueValidator.validateOrganization("or"));
        assertFalse(AdditionValueValidator.validateOrganization("12345678901234567890123456789012345678901"));
        assertTrue(AdditionValueValidator.validateOrganization("1234567890123456789012345678901234567890"));

        //uppercase test
        assertTrue(AdditionValueValidator.validateOrganization("AAAaaa"));
        assertTrue(AdditionValueValidator.validateOrganization("aaaAAA"));
        assertTrue(AdditionValueValidator.validateOrganization("aAAAAa"));

        //special character test
        assertFalse(AdditionValueValidator.validateOrganization("aaa."));
        assertFalse(AdditionValueValidator.validateOrganization("a(aa"));
        assertFalse(AdditionValueValidator.validateOrganization("!aaa"));

        //success test
        assertTrue(AdditionValueValidator.validateOrganization("pinpoint-naver"));
        assertTrue(AdditionValueValidator.validateOrganization("pinpoint-___"));
        assertTrue(AdditionValueValidator.validateOrganization("pinpoint--_naver"));
        assertTrue(AdditionValueValidator.validateOrganization("-pinpoint_naver-"));
    }

}