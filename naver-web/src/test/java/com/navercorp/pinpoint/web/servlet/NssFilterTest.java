/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.service.NssAuthService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class NssFilterTest {

    private static final String USER_HEADER_KEY = "SSO_USER";

    private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    private final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    private final FilterChain mockFilterChain = spy(FilterChain.class);

    private final NssAuthService nssAuthService = mock(NssAuthService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private NssFilter nssFilter = new NssFilter(USER_HEADER_KEY, nssAuthService, objectMapper);

    private void setupFilter() throws Exception {
        doNothing().when(mockFilterChain).doFilter(mockRequest, mockResponse);
        nssFilter.init();
    }

    private void setupFilter(List<String> authorizedUserIdPrefixes, List<String> overriddenUserIds) throws Exception {
        when(nssAuthService.getAuthorizedPrefixes()).thenReturn(authorizedUserIdPrefixes);
        when(nssAuthService.getOverrideUserIds()).thenReturn(overriddenUserIds);
        setupFilter();
    }

    @Test
    public void authorizedUserShouldPass() throws Exception {
        // Given
        final List<String> authorizedUserPrefixes = Collections.singletonList("kr1");
        final List<String> overriddenUserIds = Collections.emptyList();
        setupFilter(authorizedUserPrefixes, overriddenUserIds);

        final String authorizedUserId = "kr12345";
        mockRequest.addHeader(USER_HEADER_KEY, authorizedUserId);
        // When
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void authorizedUserShouldPass_ignoreCase() throws Exception {
        // Given
        final List<String> authorizedUserIdPrefixes = Collections.singletonList("kr1");
        final List<String> overriddenUserIds = Collections.emptyList();
        setupFilter(authorizedUserIdPrefixes, overriddenUserIds);

        final String authorizedUserId = "KR12345";
        mockRequest.addHeader(USER_HEADER_KEY, authorizedUserId);
        // When
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void unauthorizedCorporateUsersShouldBeRedirected() throws Exception {
        // Given
        final List<String> authorizedUserIdPrefixes = Collections.singletonList("kr1");
        final List<String> overriddenUserIds = Collections.emptyList();
        setupFilter(authorizedUserIdPrefixes, overriddenUserIds);

        final String unauthorizedUserId = "nb12345";
        mockRequest.addHeader(USER_HEADER_KEY, unauthorizedUserId);
        // When
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertUnauthorized();
    }

    @Test
    public void invalidUserTypeShouldBeRedirected() throws Exception {
        // Given
        final List<String> authorizedUserIdPrefixes = Collections.singletonList("kr1");
        final List<String> overriddenUserIds = Collections.emptyList();
        setupFilter(authorizedUserIdPrefixes, overriddenUserIds);

        final String unauthorizedUserId = "kr23456";
        mockRequest.addHeader(USER_HEADER_KEY, unauthorizedUserId);
        // When
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertUnauthorized();
    }

    @Test
    public void missingHeaderShouldPass() throws Exception {
        // Given
        setupFilter();
        // When
        this.nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void unauthorizedButOverriddenUsersShouldPass() throws Exception {
        // Given
        final String overriddenUserId = "someOverriddenUserId";
        final List<String> authorizedUserIdPrefixes = Collections.emptyList();
        final List<String> overriddenUserIds = Collections.singletonList(overriddenUserId);

        setupFilter(authorizedUserIdPrefixes, overriddenUserIds);
        mockRequest.addHeader(USER_HEADER_KEY, overriddenUserId);
        // When
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void shouldAuthorizeIfPrefixesAreNotDefined() throws Exception {
        // Given
        final List<String> authorizedUserIdPrefixes = Collections.emptyList();
        final List<String> overriddenUserIds = Collections.emptyList();
        setupFilter(authorizedUserIdPrefixes, overriddenUserIds);

        mockRequest.addHeader(USER_HEADER_KEY, "randomId");
        // when
        nssFilter.doFilter(mockRequest, mockResponse, mockFilterChain);
        // Then
        assertAuthorized();
    }

    private void assertAuthorized() throws Exception {
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    private void assertUnauthorized() throws Exception {
        verifyZeroInteractions(mockFilterChain);
        assertEquals(nssFilter.getUnauthorizedResponse(), mockResponse.getContentAsString());
    }
    
}
