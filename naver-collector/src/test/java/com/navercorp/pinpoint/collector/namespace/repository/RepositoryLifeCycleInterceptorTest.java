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

package com.navercorp.pinpoint.collector.namespace.repository;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class RepositoryLifeCycleInterceptorTest {

    private static final long THREE_WEEK = 1814400000L;

    @Test
    public void test() throws Throwable {
        final CallMethod callMethod = new CallMethod();
        Answer answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                callMethod.setCalled(true);
                return null;
            }
        };
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        when(proceedingJoinPoint.proceed()).thenAnswer(answer);

        long currentTime = new Date().getTime();
        List<PaaSOrganizationLifeCycle> paaSOrganizationLifeCycleList = new ArrayList<>(2);
        paaSOrganizationLifeCycleList.add(new PaaSOrganizationLifeCycle("ORG1", false,currentTime + THREE_WEEK));
        paaSOrganizationLifeCycleList.add(new PaaSOrganizationLifeCycle("ORG2", true, Long.MAX_VALUE));
        NamespaceService namespaceService = mock(NamespaceService.class);
        when(namespaceService.selectPaaSOrganizationLifeCycle()).thenReturn(paaSOrganizationLifeCycleList);

        RepositoryLifeCycleInterceptor repositoryLifeCycleInterceptor = new RepositoryLifeCycleInterceptor(namespaceService);
        repositoryLifeCycleInterceptor.updateOrganizationLifeCycle();

        ServerRequest<Object> serverRequest = mock(ServerRequest.class);
        when(serverRequest.getAttribute(NameSpaceInfo.NAMESPACE_INFO)).thenReturn(new NameSpaceInfo("ORG1", "default", "default"));
        repositoryLifeCycleInterceptor.aroundAdvice(proceedingJoinPoint, serverRequest);
        assertFalse(callMethod.isCalled());

        ServerRequest<Object> serverRequest2 = mock(ServerRequest.class);
        when(serverRequest2.getAttribute(NameSpaceInfo.NAMESPACE_INFO)).thenReturn(new NameSpaceInfo("ORG2", "default", "default"));
        repositoryLifeCycleInterceptor.aroundAdvice(proceedingJoinPoint, serverRequest2);
        assertTrue(callMethod.isCalled());
    }

    private static class CallMethod {
        boolean called = false;

        public boolean isCalled() {
            return called;
        }

        public void setCalled(boolean called) {
            this.called = called;
        }
    }

}