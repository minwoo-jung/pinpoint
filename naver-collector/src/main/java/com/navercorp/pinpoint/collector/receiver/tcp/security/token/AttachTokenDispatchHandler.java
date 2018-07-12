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

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
class AttachTokenDispatchHandler implements DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NameSpaceInfo namespaceInfo;
    private final DispatchHandler dispatchHandler;


    AttachTokenDispatchHandler(Token token, DispatchHandler dispatchHandler) {
        Assert.requireNonNull(token, "token must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.namespaceInfo = newNamespaceInfo(token);

    }

    private NameSpaceInfo newNamespaceInfo(Token token) {
        PaaSOrganizationInfo paaSOrganizationInfo = token.getPaaSOrganizationInfo();
        return new NameSpaceInfo(paaSOrganizationInfo.getOrganization(), paaSOrganizationInfo.getDatabaseName(), paaSOrganizationInfo.getHbaseNameSpace());
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        setNamespaceInfo(serverRequest);
        dispatchHandler.dispatchSendMessage(serverRequest);
    }



    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        setNamespaceInfo(serverRequest);
        dispatchHandler.dispatchRequestMessage(serverRequest, serverResponse);

    }

    private void setNamespaceInfo(ServerRequest serverRequest) {
        serverRequest.setAttribute(NameSpaceInfo.NAMESPACE_INFO, namespaceInfo);
    }

}
