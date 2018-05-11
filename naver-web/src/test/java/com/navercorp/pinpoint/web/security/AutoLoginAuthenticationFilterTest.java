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


import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.service.SecurityService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * @author minwoo.jung
 */
public class AutoLoginAuthenticationFilterTest {

    @Test
    public void doFilterInternalTest() throws ServletException, IOException {
        final String userId = "KR0000";
        final String organizationName = "navercorp";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        AutoLoginAuthenticationFilter filter = new AutoLoginAuthenticationFilter(userId);

        UserInformationAcquirer userInformationAcquirer = mock(UserInformationAcquirer.class);
        when(userInformationAcquirer.acquireUserId(any(HttpServletRequest.class))).thenReturn(userId);
        when(userInformationAcquirer.acquireOrganizationName(any(HttpServletRequest.class))).thenReturn(organizationName);
        when(userInformationAcquirer.validCheckHeader(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(filter, "userInformationAcquirer", userInformationAcquirer);

        MetaDataService metaDataService = mock(MetaDataService.class);
        ReflectionTestUtils.setField(filter, "metaDataService", metaDataService);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);

        SecurityService securityService = mock(SecurityService.class);
        ReflectionTestUtils.setField(filter, "securityService", securityService);
        when(securityService.createPinpointAuthentication(userId)).thenReturn(new PinpointAuthentication());

        filter.doFilterInternal(request, response, chain);
        assertEquals(response.getStatus(), HttpStatus.OK.value());
    }

    @Test
    public void doFilterInternal2Test() throws ServletException, IOException {
        final String userId = "KR0000";
        final String organizationName = "navercorp";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        AutoLoginAuthenticationFilter filter = new AutoLoginAuthenticationFilter(userId);

        UserInformationAcquirer userInformationAcquirer = mock(UserInformationAcquirer.class);
        when(userInformationAcquirer.acquireUserId(any(HttpServletRequest.class))).thenReturn(userId);
        when(userInformationAcquirer.acquireOrganizationName(any(HttpServletRequest.class))).thenReturn(organizationName);
        when(userInformationAcquirer.validCheckHeader(userId, organizationName)).thenReturn(false);
        ReflectionTestUtils.setField(filter, "userInformationAcquirer", userInformationAcquirer);

        filter.doFilterInternal(request, response, chain);

        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void doFilterInternal3Test() throws ServletException, IOException {
        final String userId = "KR0000";
        final String organizationName = "navercorp";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        AutoLoginAuthenticationFilter filter = new AutoLoginAuthenticationFilter(userId);

        UserInformationAcquirer userInformationAcquirer = mock(UserInformationAcquirer.class);
        when(userInformationAcquirer.acquireUserId(any(HttpServletRequest.class))).thenReturn(userId);
        when(userInformationAcquirer.acquireOrganizationName(any(HttpServletRequest.class))).thenReturn(organizationName);
        when(userInformationAcquirer.validCheckHeader(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(filter, "userInformationAcquirer", userInformationAcquirer);

        MetaDataService metaDataService = mock(MetaDataService.class);
        ReflectionTestUtils.setField(filter, "metaDataService", metaDataService);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
    }


}