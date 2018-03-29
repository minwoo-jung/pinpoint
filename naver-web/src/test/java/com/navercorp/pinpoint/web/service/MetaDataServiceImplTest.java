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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.MetaDataDao;
import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.ServletRequestEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class MetaDataServiceImplTest {


    @Test
    public void selectPaaSOrganizationInfoListTest() {
        MetaDataDao metaDataDao = mock(MetaDataDao.class);
        List<PaaSOrganizationInfo> paaSOrgInfoList = new ArrayList<PaaSOrganizationInfo>();
        PaaSOrganizationInfo naver = new PaaSOrganizationInfo("NAVER", "naver_user", "naver_pinpoint", "naver_default");
        PaaSOrganizationInfo samsung = new PaaSOrganizationInfo("SAMSUNG", "samsung_user", "samsung_pinpoint", "samsung_default");
        paaSOrgInfoList.add(naver);
        paaSOrgInfoList.add(samsung);
        when(metaDataDao.selectPaaSOrganizationInfoList()).thenReturn(paaSOrgInfoList);

        MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
        ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

        List<PaaSOrganizationInfo> paaSOrganizationInfoList = metaDataService.selectPaaSOrganizationInfoList();

        assertEquals(paaSOrganizationInfoList.size(), 2);

        assertTrue(paaSOrganizationInfoList.contains(naver));
        assertTrue(paaSOrganizationInfoList.contains(samsung));
    }

    @Test
    public void selectPaaSOrganizationInfoListForBatchPartitioning() {
        MetaDataDao metaDataDao = mock(MetaDataDao.class);
        List<PaaSOrganizationInfo> paaSOrgInfoList = new ArrayList<PaaSOrganizationInfo>();
        PaaSOrganizationInfo naver1 = new PaaSOrganizationInfo("NAVER_A", "naver_user_1", "naver_pinpoint", "naver_default");
        PaaSOrganizationInfo naver2 = new PaaSOrganizationInfo("NAVER_B", "naver_user_2", "naver_pinpoint", "naver_default");
        PaaSOrganizationInfo samsung1 = new PaaSOrganizationInfo("SAMSUNG_A", "samsung_user_1", "samsung_pinpoint", "samsung_default");
        PaaSOrganizationInfo samsung2 = new PaaSOrganizationInfo("SAMSUNG_B", "samsung_user_2", "samsung_pinpoint", "samsung_default");
        paaSOrgInfoList.add(naver1);
        paaSOrgInfoList.add(naver2);
        paaSOrgInfoList.add(samsung1);
        paaSOrgInfoList.add(samsung2);
        when(metaDataDao.selectPaaSOrganizationInfoList()).thenReturn(paaSOrgInfoList);

        MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
        ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

        List<PaaSOrganizationInfo> paaSOrganizationInfoList = metaDataService.selectPaaSOrganizationInfoListForBatchPartitioning("testBatch");

        assertEquals(paaSOrganizationInfoList.size(), 2);

        assertTrue(paaSOrganizationInfoList.contains(naver1));
        assertTrue(paaSOrganizationInfoList.contains(samsung1));
    }

    @Test
    public void allocatePaaSOrganizationInfoRequestScopeTest() {
        RequestContextListener requestContextListener = new RequestContextListener();
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(context, request);
        requestContextListener.requestInitialized(servletRequestEvent);

        try {
            MetaDataDao metaDataDao = mock(MetaDataDao.class);
            final String orgName = "test_org";
            final String userName = "test_user";
            final PaaSOrganizationInfo testPaaSOrgInfo = new PaaSOrganizationInfo(orgName, "test", "test_database", "test_hbase");
            when(metaDataDao.selectPaaSOrganizationInfo(orgName)).thenReturn(testPaaSOrgInfo);

            MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
            ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

            boolean isSuccess = metaDataService.allocatePaaSOrganizationInfoRequestScope(userName, orgName);
            assertTrue(isSuccess);

            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_REQUEST);

            assertEquals(testPaaSOrgInfo, paaSOrganizationInfo);
        } finally {
            requestContextListener.requestDestroyed(servletRequestEvent);
        }
    }

    @Test
    public void allocatePaaSOrganizationInfoRequestScope2Test() {
        RequestContextListener requestContextListener = new RequestContextListener();
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(context, request);
        requestContextListener.requestInitialized(servletRequestEvent);

        try {
            MetaDataDao metaDataDao = mock(MetaDataDao.class);
            final String orgName = "test_org";
            final String userName = "test_user";
            when(metaDataDao.selectPaaSOrganizationInfo(orgName)).thenReturn(null);

            MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
            ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

            boolean isSuccess = metaDataService.allocatePaaSOrganizationInfoRequestScope(userName, orgName);
            assertFalse(isSuccess);

            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_REQUEST);

            assertNull(paaSOrganizationInfo);
        } finally {
            requestContextListener.requestDestroyed(servletRequestEvent);
        }
    }

    @Test
    public void allocatePaaSOrganizationInfoSessionScopeTest() {
        RequestContextListener requestContextListener = new RequestContextListener();
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(context, request);
        requestContextListener.requestInitialized(servletRequestEvent);

        try {
            MetaDataDao metaDataDao = mock(MetaDataDao.class);
            final String orgName = "test_org";
            final String userName = "test_user";
            final PaaSOrganizationInfo testPaaSOrgInfo = new PaaSOrganizationInfo(orgName, "test", "test_database", "test_hbase");
            when(metaDataDao.selectPaaSOrganizationInfo(orgName)).thenReturn(testPaaSOrgInfo);

            MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
            ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

            boolean isSuccess = metaDataService.allocatePaaSOrganizationInfoSessionScope(userName, orgName);
            assertTrue(isSuccess);

            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_SESSION);

            assertEquals(testPaaSOrgInfo, paaSOrganizationInfo);
        } finally {
            requestContextListener.requestDestroyed(servletRequestEvent);
        }
    }

    @Test
    public void allocatePaaSOrganizationInfoSessionScope2Test() {
        RequestContextListener requestContextListener = new RequestContextListener();
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(context, request);
        requestContextListener.requestInitialized(servletRequestEvent);

        try {
            MetaDataDao metaDataDao = mock(MetaDataDao.class);
            final String orgName = "test_org";
            final String userName = "test_user";
            when(metaDataDao.selectPaaSOrganizationInfo(orgName)).thenReturn(null);

            MetaDataServiceImpl metaDataService = new MetaDataServiceImpl();
            ReflectionTestUtils.setField(metaDataService, "metaDataDao", metaDataDao);

            boolean isSuccess = metaDataService.allocatePaaSOrganizationInfoSessionScope(userName, orgName);
            assertFalse(isSuccess);

            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_SESSION);

            assertNull(paaSOrganizationInfo);
        } finally {
            requestContextListener.requestDestroyed(servletRequestEvent);
        }
    }

}