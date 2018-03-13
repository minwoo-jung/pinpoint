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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.ServletRequestEvent;

/**
 * @author minwoo.jung
 */
public class RequestContextInitializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestContextListener requestContextListener;
    private final ServletRequestEvent servletRequestEvent;

    public RequestContextInitializer () {
        requestContextListener = new RequestContextListener();
        MockServletContext context = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        servletRequestEvent = new ServletRequestEvent(context, request);
    }

    @Before
    public void before() {
        requestContextListener.requestInitialized(servletRequestEvent);
        StaticOrganizationInfoAllocator.allocate("test");
    }

    @After
    public void after() {
        requestContextListener.requestDestroyed(servletRequestEvent);
    }
}