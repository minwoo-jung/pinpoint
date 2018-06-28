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
import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author jaehong.kim
 */
public class NettyServerRequestWrapper implements ServerRequestWrapper {
    private final HttpHeaders headers;
    private final String rpcName;
    private final String endPoint;
    private final String remoteAddress;
    private final String acceptorHost;

    public NettyServerRequestWrapper(HttpHeaders headers, final String rpcName, final String endPoint, final String remoteAddress, final String acceptorHost) {
        this.headers = headers;
        this.rpcName = rpcName;
        this.endPoint = endPoint;
        this.remoteAddress = remoteAddress;
        this.acceptorHost = acceptorHost;
    }

    @Override
    public String getHeader(String name) {
        if (headers != null) {
            return this.headers.get(name);
        }
        return null;
    }

    @Override
    public String getRpcName() {
        return this.rpcName;
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