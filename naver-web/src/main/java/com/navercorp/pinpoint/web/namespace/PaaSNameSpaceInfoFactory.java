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

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.namespace.websocket.WebSocketContextHolder;
import com.navercorp.pinpoint.web.util.BatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author minwoo.jung
 */
public class PaaSNameSpaceInfoFactory implements NameSpaceInfoFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("batchNameSpaceInfoHolder")
    NameSpaceInfoHolder batchNameSpaceInfoHolder;

    @Autowired
    @Qualifier("requestNameSpaceInfoHolder")
    NameSpaceInfoHolder requestNameSpaceInfoHolder;

    @Autowired
    @Qualifier("webSocketNameSpaceInfoHolder")
    NameSpaceInfoHolder webSocketNameSpaceInfoHolder;

    public NameSpaceInfo getNameSpaceInfo() {
        Scope scope = checkScope();

        switch (scope) {
            case REQUEST:
                return requestNameSpaceInfoHolder.getNameSpaceInfo();
            case BATCH:
                return batchNameSpaceInfoHolder.getNameSpaceInfo();
            case WEBSOCKET:
                return webSocketNameSpaceInfoHolder.getNameSpaceInfo();
            default :
                throw new RuntimeException("can't get NameSpaceInfo from NameSpaceInfoHolder [Thread name : " + Thread.currentThread().getName() + "]");
        }
    }

    private Scope checkScope() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return Scope.REQUEST;
        } else if (WebSocketContextHolder.getAttributes() != null) {
            return Scope.WEBSOCKET;
        } else if (StepSynchronizationManager.getContext() != null) {
            return Scope.BATCH;
        } else {
            return Scope.NONE;
        }
    }

    private enum Scope {
        REQUEST, WEBSOCKET, BATCH, NONE
    }
}
