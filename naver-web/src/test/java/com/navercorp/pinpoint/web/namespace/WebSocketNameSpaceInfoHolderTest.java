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

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.namespace.websocket.WebSocketAttributes;
import com.navercorp.pinpoint.web.namespace.websocket.WebSocketContextHolder;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class WebSocketNameSpaceInfoHolderTest {

    @Test
    public void getNameSpaceInfo() {
        WebSocketAttributes webSocketAttributes  = new WebSocketAttributes(new ConcurrentHashMap<String, Object>());
        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("navercorp", "test", "pinpoint", "default");
        webSocketAttributes.setAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
        WebSocketContextHolder.setAttributes(webSocketAttributes);

        WebSocketNameSpaceInfoHolder webSocketNameSpaceInfoHolder = new WebSocketNameSpaceInfoHolder();
        NameSpaceInfo nameSpaceInfo = webSocketNameSpaceInfoHolder.getNameSpaceInfo();

        assertEquals(nameSpaceInfo.getUserId(), "test");
        assertEquals(nameSpaceInfo.getMysqlDatabaseName(), "pinpoint");
        assertEquals(nameSpaceInfo.getHbaseNamespace(), "default");

        WebSocketContextHolder.resetAttributes();
        assertTrue(WebSocketContextHolder.getAttributes() == null);
    }

    @Test(expected = Exception.class)
    public void getNameSpaceInfo2() {
        WebSocketNameSpaceInfoHolder webSocketNameSpaceInfoHolder = new WebSocketNameSpaceInfoHolder();
    }
}