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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class LocalAuthenticationProviderTest {

    @Test
    public void authenticateTest() {
        final String userId = "KR0000";
        LocalAuthenticationProvider provider = new LocalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoSessionScope(userId, "KR")).thenReturn(true);
        ReflectionTestUtils.setField(provider, "metaDataService", metaDataService);

        SecurityService securityService = mock(SecurityService.class);
        PinpointAuthentication authentication = new PinpointAuthentication(userId, "name", Collections.emptyList(), true, true);
        when(securityService.createPinpointAuthentication(userId)).thenReturn(authentication);
        ReflectionTestUtils.setField(provider, "securityService", securityService);

        Authentication auth = provider.authenticate(new UsernamePasswordAuthenticationToken(userId, "paaswd"));
        assertEquals(auth, authentication);
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        assertEquals(authorities.size(), 1);
        for (GrantedAuthority authority : authorities) {
            assertEquals(authority.getAuthority(), "ROLE_USER");
        }
    }

    @Test
    public void authenticate2Test() {
        final String userId = "KR0000";
        LocalAuthenticationProvider provider = new LocalAuthenticationProvider();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.allocatePaaSOrganizationInfoSessionScope(userId, "KR")).thenReturn(true);
        ReflectionTestUtils.setField(provider, "metaDataService", metaDataService);

        SecurityService securityService = mock(SecurityService.class);
        PinpointAuthentication authentication = new PinpointAuthentication();
        when(securityService.createPinpointAuthentication(userId)).thenReturn(authentication);
        ReflectionTestUtils.setField(provider, "securityService", securityService);

        Authentication auth = provider.authenticate(new UsernamePasswordAuthenticationToken(userId, "paaswd"));
        assertEquals(auth, authentication);
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        assertEquals(authorities.size(), 0);
    }

}