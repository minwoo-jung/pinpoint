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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleAndRequestResponseHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.io.AuthenticationTBaseLocator;

/**
 * @author Taejin Koo
 */
public class TokenEnableTcpDispatchHandler extends TcpDispatchHandler {

    private RequestResponseHandler createTokenHandler;

    public TokenEnableTcpDispatchHandler(SimpleAndRequestResponseHandler agentInfoHandler, RequestResponseHandler sqlMetaDataHandler, RequestResponseHandler apiMetaDataHandler, RequestResponseHandler stringMetaDataHandler, RequestResponseHandler createTokenHandler) {
        super(agentInfoHandler, sqlMetaDataHandler, apiMetaDataHandler, stringMetaDataHandler);
        this.createTokenHandler = createTokenHandler;
    }

    @Override
    protected RequestResponseHandler getRequestResponseHandler(ServerRequest serverRequest) {
        final Header header = serverRequest.getHeader();
        final short type = header.getType();
        if (type == AuthenticationTBaseLocator.GET_AUTHENTICATION_TOKEN) {
            return createTokenHandler;
        }

        RequestResponseHandler requestResponseHandler = super.getRequestResponseHandler(serverRequest);
        if (requestResponseHandler != null) {
            return requestResponseHandler;
        }

        return null;
    }
}