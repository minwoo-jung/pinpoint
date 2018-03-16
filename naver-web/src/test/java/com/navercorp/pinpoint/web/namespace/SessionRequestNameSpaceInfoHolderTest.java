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

package com.navercorp.pinpoint.web.namespace;

import com.navercorp.pinpoint.web.security.StaticOrganizationInfoAllocator;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.ServletRequestEvent;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class SessionRequestNameSpaceInfoHolderTest {
    @Test
    public void getNameSpaceInfo() throws Exception {
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(context, request);
        RequestContextListener requestContextListener = new RequestContextListener();
        requestContextListener.requestInitialized(servletRequestEvent);
        StaticOrganizationInfoAllocator.allocateForSessionScope("test");

        try {
            SessionRequestNameSpaceInfoHolder sessionRequestNameSpaceInfoHolder = new SessionRequestNameSpaceInfoHolder();
            NameSpaceInfo nameSpaceInfo = sessionRequestNameSpaceInfoHolder.getNameSpaceInfo();

            assertEquals(nameSpaceInfo.getUserId(), "test");
            assertEquals(nameSpaceInfo.getMysqlDatabaseName(), "pinpoint");
            assertEquals(nameSpaceInfo.getHbaseNamespace(), "default");
        } finally {
            requestContextListener.requestDestroyed(servletRequestEvent);
        }
    }

    @Test(expected = Exception.class)
    public void getNameSpaceInfo2() {
        SessionRequestNameSpaceInfoHolder dessionRequestNameSpaceInfoHolder = new SessionRequestNameSpaceInfoHolder();
    }
}