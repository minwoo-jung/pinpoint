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

package com.navercorp.pinpoint.web.security;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class LocalAuthenticationFilterTest {

    @Test
    public void doFilterInternalTest() throws ServletException, IOException {
        PinpointAuthentication authentication = new PinpointAuthentication();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        LocalAuthenticationFilter filter = new LocalAuthenticationFilter();
        filter.doFilterInternal(request, response, chain);
    }

    @Test
    public void doFilterInternal2Test() throws ServletException, IOException {
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.EMPTY_LIST, false, false);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        LocalAuthenticationFilter filter = new LocalAuthenticationFilter();
        filter.doFilterInternal(request, response, chain);
    }

    @Test
    public void doFilterInternal3Test() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        LocalAuthenticationFilter filter = new LocalAuthenticationFilter();
        filter.doFilterInternal(request, response, chain);
    }
}