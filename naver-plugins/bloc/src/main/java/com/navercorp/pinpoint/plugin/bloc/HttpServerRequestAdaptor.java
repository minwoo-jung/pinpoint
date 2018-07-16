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

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import external.org.apache.coyote.Request;
import external.org.apache.tomcat.util.buf.MessageBytes;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpServerRequestAdaptor implements RequestAdaptor<Request> {
    @Override
    public String getHeader(Request request, String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRpcName(Request request) {
        final String requestURL = request.requestURI().toString();
        return requestURL;
    }

    @Override
    public String getEndPoint(Request request) {
        String host = request.serverName().toString();
        int port = request.getServerPort();
        if (host == null) {
            return "unknown";
        }
        port = HostAndPort.getPortOrNoPort(port);
        return HostAndPort.toHostAndPortString(host, port);
    }

    @Override
    public String getRemoteAddress(Request request) {
        MessageBytes messageBytes = request.remoteAddr();
        if (messageBytes != null) {
            return messageBytes.toString();
        }
        return null;
    }

    @Override
    public String getAcceptorHost(Request request) {
        return request.serverName().toString();
    }
}
