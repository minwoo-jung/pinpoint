/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.dao.NameSpaceDao;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
class AttachTokenDispatchHandler implements DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, String> namespace;
    private final DispatchHandler dispatchHandler;


    AttachTokenDispatchHandler(Token token, DispatchHandler dispatchHandler) {
        Assert.requireNonNull(token, "token must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        namespace = newNamespace(token);

    }

    private Map<String, String> newNamespace(Token token) {
        // TODO need to change it later.
        Map<String, String> headerData = new HashMap<>();
        headerData.put("organization", "kR");
        headerData.put("databaseName", token.getNamespace());
        headerData.put("hbaseNameSpace", token.getNamespace());

        return Collections.unmodifiableMap(headerData);
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        setNamespace(serverRequest);
        dispatchHandler.dispatchSendMessage(serverRequest);
    }



    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        setNamespace(serverRequest);
        dispatchHandler.dispatchRequestMessage(serverRequest, serverResponse);

    }

    private void setNamespace(ServerRequest serverRequest) {
        // TODO conceptual code
        for (Map.Entry<String, String> entry : namespace.entrySet()) {
            serverRequest.setAttribute(entry.getKey(), entry.getValue());
        }

//        Namespace namespace = new Namespace(xx, yy, mm);
//        serverRequest.setAttribute("pinpoint.namespace", namespace);
    }

}
