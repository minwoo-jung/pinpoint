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

package com.navercorp.pinpoint.web.security.internal;

import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.service.SecurityService;
import com.navercorp.pinpoint.web.service.UserAccountService;
import com.navercorp.pinpoint.web.service.UserInformationService;
import com.navercorp.pinpoint.web.vo.UserAccount;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.exception.UserPrincipalNotFoundException;
import com.navercorp.pinpoint.web.vo.role.PermissionCollection;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class InternalAuthenticationProviderTest {

    @Test
    public void authenticateTest() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UserInformationService userInformationService = mock(UserInformationService.class);
        when(userInformationService.isExistUserId(userId)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userInformationService", userInformationService);

        UserAccountService userAccountService = mock(UserAccountService.class);
        UserAccount userAccount = new UserAccount(userId, credentials);
        when(userAccountService.selectUserAccount(userId)).thenReturn(userAccount);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userAccountService", userAccountService);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(credentials, credentials)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "passwordEncoder", passwordEncoder);

        List<UserGroup> affiliatedUserGroupList = new ArrayList<>();
        RoleInformation roleInformation = new RoleInformation("user", PermissionCollection.DEFAULT);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication("userId", "name", affiliatedUserGroupList, true, roleInformation);
        SecurityService securityService = mock(SecurityService.class);
        when(securityService.createPinpointAuthentication(userId)).thenReturn(pinpointAuthentication);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "securityService", securityService);


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        Authentication authentication = internalAuthenticationProvider.authenticate(authenticationToken);

        assertEquals(authentication, pinpointAuthentication);
    }

    @Test(expected=InternalAuthenticationServiceException.class)
    public void authenticateTest2() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(false);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test(expected=InternalAuthenticationServiceException.class)
    public void authenticateTest3() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenThrow(new RuntimeException());
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test(expected=BadCredentialsException.class)
    public void authenticateTest4() {
        String userId = "USER_ID";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test(expected=UserPrincipalNotFoundException.class)
    public void authenticateTest5() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UserInformationService userInformationService = mock(UserInformationService.class);
        when(userInformationService.isExistUserId(userId)).thenReturn(false);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userInformationService", userInformationService);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test(expected=BadCredentialsException.class)
    public void authenticateTest6() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UserInformationService userInformationService = mock(UserInformationService.class);
        when(userInformationService.isExistUserId(userId)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userInformationService", userInformationService);

        UserAccountService userAccountService = mock(UserAccountService.class);
        UserAccount userAccount = new UserAccount(userId, credentials);
        when(userAccountService.selectUserAccount(userId)).thenReturn(userAccount);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userAccountService", userAccountService);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(credentials, credentials)).thenReturn(false);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "passwordEncoder", passwordEncoder);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test(expected=InternalAuthenticationServiceException.class)
    public void authenticateTest7() {
        String userId = "userid";
        String credentials = "credentials";
        String organizationName = "organizationName";

        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "metaDataService", metaDataService);

        UserInformationService userInformationService = mock(UserInformationService.class);
        when(userInformationService.isExistUserId(userId)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userInformationService", userInformationService);

        UserAccountService userAccountService = mock(UserAccountService.class);
        UserAccount userAccount = new UserAccount(userId, credentials);
        when(userAccountService.selectUserAccount(userId)).thenReturn(userAccount);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "userAccountService", userAccountService);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(credentials, credentials)).thenReturn(true);
        ReflectionTestUtils.setField(internalAuthenticationProvider, "passwordEncoder", passwordEncoder);

        List<UserGroup> affiliatedUserGroupList = new ArrayList<>();
        RoleInformation roleInformation = new RoleInformation("user", PermissionCollection.DEFAULT);
        PinpointAuthentication pinpointAuthentication = new PinpointAuthentication("userId", "name", affiliatedUserGroupList, true, roleInformation);
        SecurityService securityService = mock(SecurityService.class);
        when(securityService.createPinpointAuthentication(userId)).thenThrow(new RuntimeException());
        ReflectionTestUtils.setField(internalAuthenticationProvider, "securityService", securityService);


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setParameter(WebAuthenticationDetails.ORGANIZATION_PARAM_NAME, organizationName);
        authenticationToken.setDetails(new WebAuthenticationDetails(mockHttpServletRequest));
        internalAuthenticationProvider.authenticate(authenticationToken);
    }

    @Test
    public void supportsTest() {
        InternalAuthenticationProvider internalAuthenticationProvider = new InternalAuthenticationProvider();
        assertTrue(internalAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }


}