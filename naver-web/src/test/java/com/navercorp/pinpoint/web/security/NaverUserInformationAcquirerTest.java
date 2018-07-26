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
import org.mockito.Mock;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class NaverUserInformationAcquirerTest {

    @Test
    public void acquireUserIdTest() {
        final String headerKey = "SSO_USER";
        final String id = "KR12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerKey, id);

        assertEquals(acquirer.acquireUserId(request), id);
    }

    @Test
    public void acquireUserId2Test() {
        final String headerKey = "SSO_USER";
        final String id = "KR1234";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(headerKey, id);
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireUserId(request), id);
    }

    @Test
    public void acquireUserId3Test() {
        final String headerKey = "SSO_USER";
        final String id = "KRN12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(headerKey, id);
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireUserId(request), id);
    }

    @Test
    public void acquireUserId4Test() {
        final String headerKey = "SSO_USER";
        final String id = "KR12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireUserId(request), "");
    }

    @Test
    public void acquireOrganizationNameTest() {
        final String headerKey = "SSO_USER";
        final String id = "KR12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(headerKey, id);
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireOrganizationName(request), "KR");
    }

    @Test
    public void acquireOrganizationName2Test() {
        final String headerKey = "SSO_USER";
        final String id = "KR12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireOrganizationName(request), "");
    }

    @Test
    public void acquireOrganizationName3Test() {
        final String headerKey = "SSO_USER";
        final String id = "KR12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals(acquirer.acquireOrganizationName(request), "");
    }

    @Test
    public void acquireOrganizationName4Test() {
        final String headerKey = "SSO_USER";
        final String id = "KR1234";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerKey, id);
        assertEquals(acquirer.acquireOrganizationName(request), "KR");
    }

    @Test
    public void acquireOrganizationName5Test() {
        final String headerKey = "SSO_USER";
        final String id = "KRN12345";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerKey, id);
        assertEquals(acquirer.acquireOrganizationName(request), "KRN");
    }

    @Test
    public void acquireOrganizationName6Test() {
        final String headerKey = "SSO_USER";
        final String id = "s";

        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(headerKey, id);
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockHttpServletRequest);
        assertEquals(acquirer.acquireOrganizationName(request), "");
    }

    @Test
    public void validCheckHeaderTest() {
        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        assertFalse(acquirer.validCheckHeader("", "organizationName"));
    }

    @Test
    public void validCheckHeader2Test() {
        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        assertFalse(acquirer.validCheckHeader("userId", ""));
    }

    @Test
    public void validCheckHeader3Test() {
        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        assertTrue(acquirer.validCheckHeader("userId", "organizationName"));
    }

    @Test
    public void getUserIdHeaderNameTest() {
        final String headerKey = "SSO_USER";
        NaverUserInformationAcquirer acquirer = new NaverUserInformationAcquirer();
        ReflectionTestUtils.setField(acquirer, "userIdHeaderName", headerKey);

        assertEquals(acquirer.getUserIdHeaderName(), headerKey);
    }
}