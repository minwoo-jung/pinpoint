/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.receiver.grpc.security.AuthTokenHolder;
import com.navercorp.pinpoint.collector.receiver.grpc.security.GrpcSecurityContext;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;

import io.grpc.StatusException;

/**
 * @author Taejin Koo
 */
public class AddNameSpaceInfoServerRequestFactory implements ServerRequestFactory {

    private static final ServerRequestFactory DEFAULT_SERVER_REQUEST_FACTORY = new DefaultServerRequestFactory();

    @Override
    public <T> ServerRequest<T> newServerRequest(Message<T> message) throws StatusException {
        ServerRequest<T> serverRequest = DEFAULT_SERVER_REQUEST_FACTORY.newServerRequest(message);

        AuthTokenHolder authTokenHolder = GrpcSecurityContext.getAuthTokenHolder();
        if (authTokenHolder == null) {
            return serverRequest;
        }

        Token token = authTokenHolder.getToken();
        if (token == null) {
            return serverRequest;
        }

        PaaSOrganizationInfo paaSOrganizationInfo = token.getPaaSOrganizationInfo();
        if (paaSOrganizationInfo == null) {
            return serverRequest;
        }

        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo(paaSOrganizationInfo.getOrganization(), paaSOrganizationInfo.getDatabaseName(), paaSOrganizationInfo.getHbaseNameSpace());
        serverRequest.setAttribute(NameSpaceInfo.NAMESPACE_INFO, nameSpaceInfo);
        return serverRequest;
    }

}
