/*
 * Copyright 2017 NAVER Corp.
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class NaverUserInfoDecoderTest {

    @Test
    public void test() {
        NaverUserInfoDecoder naverUserInfoDecoder = new NaverUserInfoDecoder("6D, 69, 73, 63, 6F, 6D, 69, 66, 64, 65, 76, 31, 32, 33, 21, 40");
        List<String> encodePhoneNumberList = new ArrayList<>(1);
        encodePhoneNumberList.add("2dbbb5fd26d1f8f2dfb1fb8d5055d07c");
        List<String> phoneNumbers = naverUserInfoDecoder.decodePhoneNumberList(encodePhoneNumberList);

        assertEquals(phoneNumbers.size(), 1);
        assertEquals("010-0000-0000", phoneNumbers.get(0));
    }
}