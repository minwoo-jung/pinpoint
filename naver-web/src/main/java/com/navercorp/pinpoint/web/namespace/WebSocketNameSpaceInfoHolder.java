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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author minwoo.jung
 */
@Component
public class WebSocketNameSpaceInfoHolder implements NameSpaceInfoHolder {

    private final NameSpaceInfo webSocketNameSpaceInfo;

    public WebSocketNameSpaceInfoHolder() {
        WebSocketAttributes attributes = WebSocketContextHolder.currentAttributes();
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        Assert.notNull(paaSOrganizationInfo, "PaaSOrganizationInfo must not be null.");

        String databaseName = paaSOrganizationInfo.getDatabaseName();
        String hbaseNameSpace = paaSOrganizationInfo.getHbaseNameSpace();
        this.webSocketNameSpaceInfo = new NameSpaceInfo(paaSOrganizationInfo.getUserId(), databaseName, hbaseNameSpace);
    }

    @Override
    public NameSpaceInfo getNameSpaceInfo() {
        return webSocketNameSpaceInfo;
    }
}
