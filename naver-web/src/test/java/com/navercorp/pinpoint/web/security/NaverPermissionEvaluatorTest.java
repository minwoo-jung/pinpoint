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

import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentParam;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class NaverPermissionEvaluatorTest {

    @Test(expected = UnsupportedOperationException.class)
    public void hasPermissionTest() {
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();
        assertTrue(naverPermissionEvaluator.hasPermission(null, null, null));
    }

    @Test
    public void hasPermission2Test() {
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false, true);
        RoleInformation roleInformation = new RoleInformation("roleId", new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT));
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "name", Collections.emptyList(), true, roleInformation);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        assertTrue(naverPermissionEvaluator.hasPermission(authentication, new String(), NaverPermissionEvaluator.APPLICATION, NaverPermissionEvaluator.INSPECTOR));
    }

    @Test
    public void hasPermission4Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "applicationId", Collections.emptyList(), true, RoleInformation.UNASSIGNED_ROLE);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        ServerMapDataFilter serverMapDataFilter = mock(ServerMapDataFilter.class);
        when(serverMapDataFilter.filter(any(Application.class))).thenReturn(false);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "serverMapDataFilter", serverMapDataFilter);
        assertTrue(naverPermissionEvaluator.hasPermission(authentication, "applicationId", NaverPermissionEvaluator.APPLICATION, NaverPermissionEvaluator.INSPECTOR));
    }

    @Test
    public void hasPermission5Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "applicationId", Collections.emptyList(), true, RoleInformation.UNASSIGNED_ROLE);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        ServerMapDataFilter serverMapDataFilter = mock(ServerMapDataFilter.class);
        when(serverMapDataFilter.filter(any(Application.class))).thenReturn(false);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "serverMapDataFilter", serverMapDataFilter);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName("applicationName");
        when(agentInfoService.getAgentInfo(any(String.class), any(Long.class))).thenReturn(agentInfo);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "agentInfoService", agentInfoService);

        assertTrue(naverPermissionEvaluator.hasPermission(authentication, new AgentParam("agentId", 1000L), NaverPermissionEvaluator.AGENT_PARAM, NaverPermissionEvaluator.INSPECTOR));
    }

    @Test
    public void hasPermission6Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "applicationId", Collections.emptyList(), true, RoleInformation.UNASSIGNED_ROLE);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        ServerMapDataFilter serverMapDataFilter = mock(ServerMapDataFilter.class);
        when(serverMapDataFilter.filter(any(Application.class))).thenReturn(false);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "serverMapDataFilter", serverMapDataFilter);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName("applicationName");
        when(agentInfoService.getAgentInfo(any(String.class), any(Long.class))).thenReturn(agentInfo);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "agentInfoService", agentInfoService);

        assertFalse(naverPermissionEvaluator.hasPermission(authentication, new AgentParam("agentId", 1000L), "", ""));
    }

    @Test
    public void hasPermission7Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "applicationId", Collections.emptyList(), true, RoleInformation.UNASSIGNED_ROLE);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        ServerMapDataFilter serverMapDataFilter = mock(ServerMapDataFilter.class);
        when(serverMapDataFilter.filter(any(Application.class))).thenReturn(false);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "serverMapDataFilter", serverMapDataFilter);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName("applicationName");
        when(agentInfoService.getAgentInfo(any(String.class), any(Long.class))).thenReturn(agentInfo);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "agentInfoService", agentInfoService);

        assertFalse(naverPermissionEvaluator.hasPermission(authentication, new AgentParam("agentId", 1000L), "", ""));
    }

    @Test
    public void hasPermission8Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("userId", "applicationId", Collections.emptyList(), true, RoleInformation.UNASSIGNED_ROLE);
        NaverPermissionEvaluator naverPermissionEvaluator = new NaverPermissionEvaluator();

        ServerMapDataFilter serverMapDataFilter = mock(ServerMapDataFilter.class);
        when(serverMapDataFilter.filter(any(Application.class))).thenReturn(false);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "serverMapDataFilter", serverMapDataFilter);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName("applicationName");
        when(agentInfoService.getAgentInfo(any(String.class), any(Long.class))).thenReturn(agentInfo);
        ReflectionTestUtils.setField(naverPermissionEvaluator, "agentInfoService", agentInfoService);

        assertFalse(naverPermissionEvaluator.hasPermission(authentication, new AgentParam("agentId", 1000L), "", NaverPermissionEvaluator.INSPECTOR));
    }
}