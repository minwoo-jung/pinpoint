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
import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import com.navercorp.pinpoint.web.security.UserInformationAcquirer;
import com.navercorp.pinpoint.web.service.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class WebSocketContextHandlerDecorator extends WebSocketHandlerDecorator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected UserInformationAcquirer userInformationAcquirer;

    public WebSocketContextHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
        Objects.requireNonNull(delegate, "WebSocketHandler must not be null");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        initialized(session);

        try {
            super.handleMessage(session, message);
        } finally {
            destroyed();
        }
    }

    private void initialized(WebSocketSession session) {
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) session.getAttributes().get(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        WebSocketAttributes webSocketAttributes  = new WebSocketAttributes(new ConcurrentHashMap<String, Object>());
        webSocketAttributes.setAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
        WebSocketContextHolder.setAttributes(webSocketAttributes);
    }

    private void destroyed() {
        WebSocketContextHolder.resetAttributes();
    }
}
