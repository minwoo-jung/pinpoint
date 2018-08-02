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

package com.navercorp.pinpoint.collector.namespace;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class RequestContextHolderTest {

    @Test
    public void test() {
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("navercorp", "pinpoint", "default");
        RequestAttributes requestAttributes = new RequestAttributes(new ConcurrentHashMap<>());
        requestAttributes.setAttribute(NameSpaceInfo.NAMESPACE_INFO, nameSpaceInfo);
        RequestContextHolder.setAttributes(requestAttributes);

        RequestAttributes attributes = RequestContextHolder.currentAttributes();
        NameSpaceInfo npInfo = (NameSpaceInfo) attributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
        assertEquals(nameSpaceInfo, npInfo);

        RequestContextHolder.resetAttributes();
        assertNull(RequestContextHolder.getAttributes());

        try {
            RequestContextHolder.currentAttributes();
        } catch (Exception e) {
            return;
        }
        fail();
    }
}