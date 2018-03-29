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
package com.navercorp.pinpoint.web.namespace.websocket;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.security.UserInformationAcquirer;
import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.websocket.CustomHandshakeInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class PaaSOrganizationInfoCreator implements CustomHandshakeInterceptor {

    private final static String ERROR_MESSAGE_USER_INFO_EMPTY = "{\"error code\" : \"401\", \"error message\" : \"error occurred in login process. May be userId or organization is empty.\"}";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    protected UserInformationAcquirer userInformationAcquirer;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        final String userId = userInformationAcquirer.acquireUserId(request);
        final String organizationName = userInformationAcquirer.acquireOrganizationName(request);
        if (userInformationAcquirer.validCheckHeader(userId, organizationName) == false) {
            logger.error("error occurred in checking userId({}), organizationName({}). Maybe userId or organizationName is null.", userId, organizationName);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getBody().write(ERROR_MESSAGE_USER_INFO_EMPTY.getBytes(UTF_8));
            return false;
        }

        PaaSOrganizationInfo paaSOrganizationInfo = metaDataService.selectPaaSOrganizationInfo(userId, organizationName);
        attributes.put(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
