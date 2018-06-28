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

package com.navercorp.pinpoint.plugin.line;

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * @author jaehong.kim
 */
public class LineServerRequestWrapper implements ServerRequestWrapper {
    private HttpRequest request;
    private final String endPoint;
    private final String remoteAddress;
    private final String acceptorHost;

    public LineServerRequestWrapper(final HttpRequest request, final String endPoint, final String remoteAddress, final String acceptorHost) {
        this.request = Assert.requireNonNull(request, "request must not be null");
        this.endPoint = endPoint;
        this.remoteAddress = remoteAddress;
        this.acceptorHost = acceptorHost;
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public String getRpcName() {
        return request.getUri();
    }

    @Override
    public String getEndPoint() {
        return this.endPoint;
    }

    @Override
    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public String getAcceptorHost() {
        return this.acceptorHost;
    }
}
