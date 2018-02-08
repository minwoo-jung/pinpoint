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

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Map;

/**
 * @author jaehong.kim
 */
public class LucyNetServerRequestTrace implements ServerRequestTrace {

    private final Map<String, String> pinpointOptions;
    private final String rpcName;
    private final String endPoint;
    private final String remoteAddress;
    private final String acceptorHost;


    public LucyNetServerRequestTrace(final Map<String, String> pinpointOptions, final String rpcName, final String endPoint, final String remoteAddress, final String acceptorHost) {
        this.pinpointOptions = Assert.requireNonNull(pinpointOptions, "pinpointOptions must not be null");
        this.rpcName = rpcName;
        this.endPoint = endPoint;
        this.remoteAddress = remoteAddress;
        this.acceptorHost = acceptorHost;
    }

    @Override
    public String getHeader(String name) {
        final String value = pinpointOptions.get(name);
        if (StringUtils.hasLength(value)) {
            return value;
        }
        return null;
    }

    @Override
    public void setHeader(String name, String value) {
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
