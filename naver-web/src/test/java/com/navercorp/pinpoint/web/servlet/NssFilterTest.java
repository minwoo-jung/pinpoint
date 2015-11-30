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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.Filter;
import javax.servlet.FilterChain;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class NssFilterTest {
    /*
    # NSS Configuration
    nss.user.header.key=SSO_USER
    nss.override.id=kr99999
    # Allowed prefixes
    nss.corportaion.prefix=KR,NB,NT,IT,CA,WM,LP,JP,LZ
    nss.user.type=1,7
     */
    private static final String AUTHORIZED_USER_ID_UPPER = "KR10000";
    private static final String AUTHORIZED_USER_ID_LOWER = "kr10000";

    private static final String UNAUTHORIZED_USER_ID_CORP = "AA00000";
    private static final String UNAUTHORIZED_USER_ID_TYPE = "kr50000";

    private static final String EMPTY_USER_ID = "";
    private static final String OVERRIDDEN_USER_ID = "kr99999";

    @Value("#{pinpointWebProps['nss.user.header.key'] ?: 'SSO_USER'}")
    private String userHeaderKey;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Autowired
    private Filter nssFilter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        doNothing().when(this.filterChain).doFilter(this.request, this.response);
    }

    @Test
    public void authorized_user_should_pass_upper() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, AUTHORIZED_USER_ID_UPPER);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void authorized_user_should_pass_lower() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, AUTHORIZED_USER_ID_LOWER);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void unauthorized_corporate_user_should_be_redirected() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, UNAUTHORIZED_USER_ID_CORP);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertUnauthorized();
    }

    @Test
    public void unauthorized_user_type_should_be_redirected() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, UNAUTHORIZED_USER_ID_TYPE);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertUnauthorized();
    }

    @Test
    public void missing_header_should_pass() throws Exception {
        // Given
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertAuthorized();
    }

    @Test
    public void empty_header_value_should_be_redirected() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, EMPTY_USER_ID);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertUnauthorized();
    }

    @Test
    public void unauthorized_but_override_users_should_pass() throws Exception {
        // Given
        this.request.addHeader(this.userHeaderKey, OVERRIDDEN_USER_ID);
        // When
        this.nssFilter.doFilter(this.request, this.response, this.filterChain);
        // Then
        assertAuthorized();
    }

    private void assertAuthorized() throws Exception {
        verify(this.filterChain, times(1)).doFilter(this.request, this.response);
        assertNull(this.response.getHeader(NssFilter.UNAUTHORIZED_RESPONSE_HEADER_KEY));
    }

    private void assertUnauthorized() throws Exception {
        verifyZeroInteractions(this.filterChain);
        assertEquals(NssFilter.UNAUTHORIZED_RESPONSE_HEADER_VALUE, this.response.getHeader(NssFilter.UNAUTHORIZED_RESPONSE_HEADER_KEY));
    }

}
