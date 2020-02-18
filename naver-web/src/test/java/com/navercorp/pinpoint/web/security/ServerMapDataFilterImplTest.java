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

import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterScanResult;
import com.navercorp.pinpoint.web.applicationmap.DefaultApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.CreateType;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.exception.AuthorityException;
import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.role.PermissionCollection;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAdministration;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAlarm;
import com.navercorp.pinpoint.web.vo.role.PermsGroupAppAuthorization;
import com.navercorp.pinpoint.web.vo.role.PermsGroupUserGroup;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountHandler;
import com.navercorp.pinpoint.web.websocket.message.RequestMessage;

import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * @author minwoo.jung
 */
public class ServerMapDataFilterImplTest {

    private static final Range TEST_RANGE = new Range(1000L, 1100L);

    @Test
    public void filterTest() {
        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        assertTrue(serverMapDataFilter.filter(new Application("application", ServiceType.STAND_ALONE)));
    }

    @Test
    public void filter2Test() {
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false, true);
        RoleInformation roleInformation = new RoleInformation("roleId", new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, roleInformation);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        assertFalse(serverMapDataFilter.filter(new Application("application", ServiceType.STAND_ALONE)));
    }

    @Test
    public void filter3Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("applicationId", Collections.emptyList());
        authentication.addApplicationConfiguration(applicationConfiguration);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();

        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";
        assertFalse(serverMapDataFilter.filter(new Application("applicationId", ServiceType.STAND_ALONE)));
    }

    @Test
    public void filter4Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("applicationId", appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);


        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();

        assertFalse(serverMapDataFilter.filter(new Application("applicationId", ServiceType.STAND_ALONE)));
    }

    @Test
    public void filter5Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";

        List<UserGroup> userGroupList = new ArrayList<>();
        userGroupList.add(new UserGroup("0", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        appUserGroupAuthList.add(appUserGroupAuth1);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("applicationId", appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);


        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);


        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        ApplicationConfigService applicationConfigService = mock(ApplicationConfigService.class);

        ReflectionTestUtils.setField(serverMapDataFilter, "applicationConfigService", applicationConfigService);

        assertTrue(serverMapDataFilter.filter(new Application("applicationId", ServiceType.STAND_ALONE)));
    }

    private AppAuthConfiguration newServerMapAppAuthConfiguration() {
        return new AppAuthConfiguration(false, false, false, true);
    }

    @Test
    public void filter6Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";

        List<UserGroup> userGroupList = new ArrayList<>();
        userGroupList.add(new UserGroup("0", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("applicationId", appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);


        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();

        assertFalse(serverMapDataFilter.filter(new Application("applicationId", ServiceType.STAND_ALONE)));
    }

    @Test
    public void filter7Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";

        List<UserGroup> userGroupList = new ArrayList<>();
        userGroupList.add(new UserGroup("0", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("applicationId", appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);


        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(null, null, null, null, authentication);
        Map<String, String> param = new HashMap<>();
        param.put(ActiveThreadCountHandler.APPLICATION_NAME_KEY, applicationId);
        RequestMessage requestMessage = new RequestMessage("command", param);

        assertFalse(serverMapDataFilter.filter(standardWebSocketSession, requestMessage));
    }

    @Test
    public void filter8Test() {
        final String applicationId = "applicationId";
        final String userGroupId = "userGroupId";

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(null, null, null, null, null);
        Map<String, String> param = new HashMap<>();
        param.put(ActiveThreadCountHandler.APPLICATION_NAME_KEY, applicationId);
        RequestMessage requestMessage = new RequestMessage("command", param);

        assertTrue(serverMapDataFilter.filter(standardWebSocketSession, requestMessage));
    }

    @Test
    public void dataFilteringTest() {
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false, true);
        RoleInformation roleInformation = new RoleInformation("roleId", new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, roleInformation);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application("application1", ServiceType.STAND_ALONE));
        Node node2 = new Node(new Application("application1", ServiceType.STAND_ALONE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        nodeList.addNode(new Node(new Application("application2", ServiceType.STAND_ALONE)));
        LinkList linkList = new LinkList();
        linkList.addLink(new Link(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, new LinkList());
        assertEquals(serverMapDataFilter.dataFiltering(map), map);
    }

    @Test
    public void dataFiltering2Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));

        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);

        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        Object[] links = defaultApplicationMap.getLinks().toArray();
        Link link = (Link) links[0];
        assertEquals(link.getFrom(), node1);
        assertEquals(link.getTo(), node2);

        assertEquals(defaultApplicationMap.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(defaultApplicationMap.getNodes().toArray(new Node[2]));
        assertTrue(nodes.contains(node1));
        assertTrue(nodes.contains(node2));
    }

    @Test
    public void dataFiltering3Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);

        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        assertEquals(defaultApplicationMap.getNodes().size(), 2);
    }

    @Test
    public void dataFiltering4Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMapWithScatterData appMapWithScatterData = new ApplicationMapWithScatterData(map, Collections.emptyMap());
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(appMapWithScatterData);

        ApplicationMapWithScatterData applicationMapWithScatterData = (ApplicationMapWithScatterData)applicationMap;

        assertEquals(applicationMapWithScatterData.getLinks().size(), 1);
        Object[] links = applicationMapWithScatterData.getLinks().toArray();
        Link link = (Link) links[0];
        assertEquals(link.getFrom(), node1);
        Node to = link.getTo();
        assertEquals(to.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(applicationMapWithScatterData.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(applicationMapWithScatterData.getNodes().toArray(new Node[2]));
        assertTrue(nodes.contains(node1));
        assertFalse(nodes.contains(node2));
    }

    @Test
    public void dataFiltering5Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMapWithScatterScanResult appMapWithScatterScanResult = new ApplicationMapWithScatterScanResult(map, Collections.emptyList());
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(appMapWithScatterScanResult);

        ApplicationMapWithScatterScanResult applicationMapWithScatterScanResult = (ApplicationMapWithScatterScanResult)applicationMap;

        assertEquals(applicationMapWithScatterScanResult.getLinks().size(), 1);
        Object[] links = applicationMapWithScatterScanResult.getLinks().toArray();
        Link link = (Link) links[0];
        assertEquals(link.getFrom(), node1);
        Application toApplication = link.getTo().getApplication();
        assertEquals(toApplication.getName(), applicationId2);
        assertEquals(toApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(applicationMapWithScatterScanResult.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(applicationMapWithScatterScanResult.getNodes().toArray(new Node[2]));
        assertTrue(nodes.contains(node1));
        assertFalse(nodes.contains(node2));

    }

    @Test(expected = AuthorityException.class)
    public void dataFiltering6Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        serverMapDataFilter.dataFiltering(new ApplicationMap() {
            @Override
            public Collection<Node> getNodes() {
                return null;
            }

            @Override
            public Collection<Link> getLinks() {
                return null;
            }
        });
    }

    @Test
    public void dataFiltering7Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), new AppAuthConfiguration());
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.UNKNOWN));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.UNKNOWN), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMapWithScatterScanResult appMapWithScatterScanResult = new ApplicationMapWithScatterScanResult(map, Collections.emptyList());
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(appMapWithScatterScanResult);

        ApplicationMapWithScatterScanResult applicationMapWithScatterScanResult = (ApplicationMapWithScatterScanResult)applicationMap;

        assertEquals(applicationMapWithScatterScanResult.getLinks().size(), 1);
        Object[] links = applicationMapWithScatterScanResult.getLinks().toArray();
        Link link = (Link) links[0];
        assertEquals(link.getFrom(), node1);
        Application toApplication = link.getTo().getApplication();
        assertEquals(toApplication.getName(), applicationId2);
        assertEquals(toApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(applicationMapWithScatterScanResult.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(applicationMapWithScatterScanResult.getNodes().toArray(new Node[2]));
        assertTrue(nodes.contains(node1));
        assertFalse(nodes.contains(node2));
    }

    @Test
    public void dataFiltering8Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration1 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration2 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);

        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        Object[] links = defaultApplicationMap.getLinks().toArray();
        Link link = (Link) links[0];
        Application fromApplication = link.getFrom().getApplication();
        assertEquals(fromApplication.getName(), applicationId);
        assertEquals(fromApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        Application toApplication = link.getTo().getApplication();
        assertEquals(toApplication.getName(), applicationId2);
        assertEquals(toApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(defaultApplicationMap.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(defaultApplicationMap.getNodes().toArray(new Node[2]));
        assertFalse(nodes.contains(node1));
        assertFalse(nodes.contains(node2));
    }

    @Test
    public void dataFiltering9Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration1 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration2 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        LinkList linkList = new LinkList();
        linkList.addLink(createTestLink(CreateType.Source, node1, node2, TEST_RANGE));
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);

        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        Object[] links = defaultApplicationMap.getLinks().toArray();
        Link link = (Link) links[0];
        Application fromApplication = link.getFrom().getApplication();
        assertEquals(fromApplication.getName(), applicationId);
        assertEquals(fromApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        Application toApplication = link.getTo().getApplication();
        assertEquals(toApplication.getName(), applicationId2);
        assertEquals(toApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(defaultApplicationMap.getNodes().size(), 0);
    }

    @Test
    public void dataFiltering10Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration1 = new AppAuthConfiguration();

        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration2 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE),
                TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        Link link = createTestLink(CreateType.Source, node1, node2, TEST_RANGE);
        linkList.addLink(link);

        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);
        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        Object[] links = defaultApplicationMap.getLinks().toArray();
        Link returnLink = (Link) links[0];
        assertEquals(returnLink.getFrom(), node1);
        Application toApplication = returnLink.getTo().getApplication();
        assertEquals(toApplication.getName(), applicationId2);
        assertEquals(toApplication.getServiceType(), ServiceType.UNAUTHORIZED);

        assertEquals(defaultApplicationMap.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(defaultApplicationMap.getNodes().toArray(new Node[2]));
        assertTrue(nodes.contains(node1));
        assertFalse(nodes.contains(node2));
    }

    @Test
    public void dataFiltering11Test() {
        final String applicationId = "applicationId";
        final String applicationId2 = "applicationId2";
        final String userGroupId = "userGroupId";

        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.emptyList(), false, RoleInformation.UNASSIGNED_ROLE);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration1 = newServerMapAppAuthConfiguration();
        AppUserGroupAuth appUserGroupAuth1 = new AppUserGroupAuth(applicationId, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        AppUserGroupAuth appUserGroupAuth2 = new AppUserGroupAuth(applicationId, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration1);
        appUserGroupAuthList.add(appUserGroupAuth1);
        appUserGroupAuthList.add(appUserGroupAuth2);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(applicationId, appUserGroupAuthList);
        authentication.addApplicationConfiguration(applicationConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList2 = new ArrayList<>();
        AppAuthConfiguration appAuthConfiguration2 = new AppAuthConfiguration();

        AppUserGroupAuth appUserGroupAuth2_1 = new AppUserGroupAuth(applicationId2, userGroupId, AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        AppUserGroupAuth appUserGroupAuth2_2 = new AppUserGroupAuth(applicationId2, "guest", AppUserGroupAuth.Position.MANAGER.getName(), appAuthConfiguration2);
        appUserGroupAuthList2.add(appUserGroupAuth2_1);
        appUserGroupAuthList2.add(appUserGroupAuth2_2);
        ApplicationConfiguration applicationConfiguration2 = new ApplicationConfiguration(applicationId2, appUserGroupAuthList2);
        authentication.addApplicationConfiguration(applicationConfiguration2);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        NodeList nodeList = new NodeList();
        Node node1 = new Node(new Application(applicationId, ServiceType.STAND_ALONE));
        node1.setNodeHistogram(new NodeHistogram(new Application(applicationId, ServiceType.STAND_ALONE), TEST_RANGE));
        Node node2 = new Node(new Application(applicationId2, ServiceType.STAND_ALONE));
        node2.setNodeHistogram(new NodeHistogram(new Application(applicationId2, ServiceType.STAND_ALONE), TEST_RANGE));
        nodeList.addNode(node1);
        nodeList.addNode(node2);
        LinkList linkList = new LinkList();
        Link link = createTestLink(CreateType.Source, node1, node2, TEST_RANGE);
        linkList.addLink(link);
        ApplicationMap map = new DefaultApplicationMap(TEST_RANGE, nodeList, linkList);
        ApplicationMap applicationMap = serverMapDataFilter.dataFiltering(map);

        DefaultApplicationMap defaultApplicationMap = (DefaultApplicationMap)applicationMap;

        assertEquals(defaultApplicationMap.getLinks().size(), 1);
        Object[] links = defaultApplicationMap.getLinks().toArray();
        Link returnLink = (Link) links[0];
        Application fromApplication = returnLink.getFrom().getApplication();
        assertEquals(fromApplication.getName(), applicationId);
        assertEquals(fromApplication.getServiceType(), ServiceType.UNAUTHORIZED);
        assertEquals(returnLink.getTo(), node2);

        assertEquals(defaultApplicationMap.getNodes().size(), 2);
        List<Node> nodes = Arrays.asList(defaultApplicationMap.getNodes().toArray(new Node[2]));
        assertFalse(nodes.contains(node1));
        assertTrue(nodes.contains(node2));
    }

    @Test
    public void getCloseStatusTest() {
        Map<String, String> param = new HashMap<>();
        param.put("key", "value");
        param.put("key2", "value2");
        RequestMessage requestMessage = new RequestMessage("command", param);

        ServerMapDataFilterImpl serverMapDataFilter = new ServerMapDataFilterImpl();
        CloseStatus closeStatus = serverMapDataFilter.getCloseStatus(requestMessage);
        assertEquals(closeStatus.getCode(), 1008);
    }

    private Link createTestLink(CreateType createType, Node fromNode, Node toNode, Range range) {
        LinkCallDataMap source = new LinkCallDataMap();
        source.addCallData("sourceAgentId", fromNode.getServiceType(), "targetId", toNode.getServiceType(), createHistogram(fromNode));

        Link link = new Link(createType, fromNode, toNode, range);
        link.addSource(source);

        return link;
    }

    private Collection<TimeHistogram> createHistogram(Node node) {
        TimeHistogram histogram = new TimeHistogram(node.getServiceType(), System.currentTimeMillis());

        int elapsedTime = ThreadLocalRandom.current().nextInt(1, 3000);
        histogram.addCallCountByElapsedTime(elapsedTime, false);

        return Arrays.asList(histogram);
    }

}