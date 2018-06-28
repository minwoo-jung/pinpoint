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

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import external.org.apache.coyote.Request;

/**
 * @author jaehong.kim
 */
public class HttpServerRequestWrapper implements ServerRequestWrapper {
    private final Request request;

    public HttpServerRequestWrapper(Request request) {
        this.request = Assert.requireNonNull(request, "request must not be null");
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRpcName() {
        final String requestURL = request.requestURI().toString();
        return requestURL;
    }

    @Override
    public String getEndPoint() {
        String host = request.serverName().toString();
        int port = request.getServerPort();
        if (host == null) {
            return "unknown";
        }
        port = HostAndPort.getPortOrNoPort(port);
        return HostAndPort.toHostAndPortString(host, port);
    }

    @Override
    public String getRemoteAddress() {
        if (request.remoteAddr() != null) {
            return request.remoteAddr().toString();
        }
        return null;
    }

    @Override
    public String getAcceptorHost() {
        return request.serverName().toString();
    }
}
