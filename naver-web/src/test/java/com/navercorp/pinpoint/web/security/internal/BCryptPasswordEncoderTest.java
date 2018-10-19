/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.web.security.internal;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author minwoo.jung
 */
public class BCryptPasswordEncoderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    public void test() {
        String encodedString  = encoder.encode("KR14966");
        logger.info(encodedString);
        assertTrue(encoder.matches("minwoo_password", "$2a$15$cYbNwf0.AmAoUzsEYlVyc.Hfc4jkfgTX9kboc1ZPVk/Ehe21cRhZe"));
        assertTrue(encoder.matches("minwoo_password", "$2a$15$854zG16mYBsQ./N.50D60O47mhVTEBweJpJnpktOwwthX2JT6VeBq"));
    }
}
