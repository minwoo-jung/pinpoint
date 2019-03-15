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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.service.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class MetaDataFilterImplTest {

    @Test
    public void filterNotExistAuthenticationTest() {
        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        Align align = new SpanAlign(new SpanBo());
        MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.API;
        boolean result = metaDataFilter.filter(align, metaData);
        assertTrue(result);
    }

    @Test
    public void filterPinpointManagerTest() {
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false, true);
        RoleInformation roleInformation = new RoleInformation("roleId", new PermissionCollection(PermsGroupAdministration.DEFAULT, permsGroupAppAuthorization, PermsGroupAlarm.DEFAULT, PermsGroupUserGroup.DEFAULT));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.EMPTY_LIST, false, roleInformation);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            Align align = new SpanAlign(new SpanBo());
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.API;
            boolean result = metaDataFilter.filter(align, metaData);
            assertFalse(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterEmptyUserGroupTest() {
        final String applicationId =  "applicationId";
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.EMPTY_LIST, false, RoleInformation.UNASSIGNED_ROLE);
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, "testGroupId", "manager", new AppAuthConfiguration());
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, Collections.EMPTY_LIST));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.API;
            boolean result = metaDataFilter.filter(align, metaData);
            assertFalse(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterSqlMetaDataTest() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", new AppAuthConfiguration());
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.SQL;
            boolean result = metaDataFilter.filter(align, metaData);
            assertFalse(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterSqlMetaData2Test() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration();
        appAuthConfiguration.setSqlMetaData(true);
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", appAuthConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.SQL;
            boolean result = metaDataFilter.filter(align, metaData);
            assertTrue(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterApiMetaDataTest() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", new AppAuthConfiguration());
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.API;
            boolean result = metaDataFilter.filter(align, metaData);
            assertFalse(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterApiMetaData2Test() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration();
        appAuthConfiguration.setApiMetaData(true);
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", appAuthConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.API;
            boolean result = metaDataFilter.filter(align, metaData);
            assertTrue(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterParamMetaDataTest() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false,  RoleInformation.UNASSIGNED_ROLE);

        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", new AppAuthConfiguration());
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.PARAM;
            boolean result = metaDataFilter.filter(align, metaData);
            assertFalse(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterParamMetaData2Test() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration();
        appAuthConfiguration.setParamMetaData(true);
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", appAuthConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            MetaDataFilter.MetaData metaData = MetaDataFilter.MetaData.PARAM;
            boolean result = metaDataFilter.filter(align, metaData);
            assertTrue(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void filterMetaDataNullTest() {
        final String applicationId =  "applicationId";
        final String userGroupId = "testGroupId";

        List<UserGroup> userGroupList = new ArrayList<>(1);
        userGroupList.add(new UserGroup("1", userGroupId));
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", userGroupList, false, RoleInformation.UNASSIGNED_ROLE);

        AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration();
        appAuthConfiguration.setApiMetaData(true);
        AppUserGroupAuth appUserGroupAuth = new AppUserGroupAuth(applicationId, userGroupId, "manager", appAuthConfiguration);
        List<AppUserGroupAuth> appUserGroupAuthList = new ArrayList<>(1);
        appUserGroupAuthList.add(appUserGroupAuth);
        authentication.addApplicationConfiguration(new ApplicationConfiguration(applicationId, appUserGroupAuthList));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
            SpanBo spanBo = new SpanBo();
            spanBo.setApplicationId(applicationId);
            Align align = new SpanAlign(spanBo);
            boolean result = metaDataFilter.filter(align, null);
            assertTrue(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void createAnnotationBoTest() {
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.EMPTY_LIST, false, RoleInformation.UNASSIGNED_ROLE);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        SpanBo spanBo = new SpanBo();
        Align align = new SpanAlign(spanBo);
        AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(align, MetaDataFilter.MetaData.PARAM);
        assertNull(annotationBo);
    }

    @Test
    public void createAnnotationBo2Test() {
        PinpointAuthentication authentication = new PinpointAuthentication("KR0000", "name", Collections.EMPTY_LIST, false, RoleInformation.UNASSIGNED_ROLE);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        SpanBo spanBo = new SpanBo();
        Align align = new SpanAlign(spanBo);
        AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(align, MetaDataFilter.MetaData.SQL);
        assertFalse(annotationBo.isAuthorized());
    }

    @Test
    public void replaceAnnotationBoTest() {
        SpanBo spanBo = new SpanBo();
        Align align = new SpanAlign(spanBo);
        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        metaDataFilter.replaceAnnotationBo(align, MetaDataFilter.MetaData.SQL);
    }

    @Test
    public void replaceAnnotationBo2Test() {
        List<AnnotationBo> annotationBoList = new ArrayList<>();
        AnnotationBo annotationBo1 = new AnnotationBo();
        annotationBo1.setKey(AnnotationKey.HTTP_URL.getCode());
        annotationBo1.setAuthorized(true);
        annotationBo1.setValue("http://navercorp.com");
        annotationBoList.add(annotationBo1);

        AnnotationBo annotationBo2 = new AnnotationBo();
        annotationBo2.setKey(AnnotationKey.HTTP_PARAM.getCode());
        annotationBo2.setAuthorized(true);
        annotationBo2.setValue("annotation3");
        annotationBoList.add(annotationBo2);

        AnnotationBo annotationBo3 = new AnnotationBo();
        annotationBo3.setKey(0);
        annotationBo3.setAuthorized(true);
        annotationBo3.setValue("annotation3");
        annotationBoList.add(annotationBo3);

        SpanBo spanBo = new SpanBo();
        spanBo.setAnnotationBoList(annotationBoList);
        Align align = new SpanAlign(spanBo);

        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        metaDataFilter.replaceAnnotationBo(align, MetaDataFilter.MetaData.PARAM);

        assertFalse(annotationBo1.isAuthorized());
        assertEquals(annotationBo1.getValue(), "http://navercorp.com");

        assertFalse(annotationBo2.isAuthorized());
        assertEquals(annotationBo2.getValue(), "you don't have authorization for null.");

        assertFalse(annotationBo3.isAuthorized());
        assertEquals(annotationBo3.getValue(), "annotation3");
    }

    @Test
    public void createRecordTest() {
        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new TransactionId("agentId", 1000L, 1000L));
        Align align = new SpanAlign(spanBo);
        CallTreeNode node = new CallTreeNode(null, align);
        AnnotationKeyMatcherService annotationKeyMatcherService = mock(AnnotationKeyMatcherService.class);
        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        AnnotationKeyRegistryService annotationKeyRegistryService = mock(AnnotationKeyRegistryService.class);
        ProxyRequestTypeRegistryService proxyRequestTypeRegistryService = mock(ProxyRequestTypeRegistryService.class);
        RecordFactory factory = new RecordFactory(annotationKeyMatcherService, registry, annotationKeyRegistryService, proxyRequestTypeRegistryService);

        MetaDataFilterImpl metaDataFilter = new MetaDataFilterImpl();
        Record record = metaDataFilter.createRecord(node, factory);
        assertEquals(record.getTitle(), "you don't have authorization for null.");
    }
}