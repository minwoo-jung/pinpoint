/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
@Profile("tokenAuthentication")
@Configuration
public class TokenConfig {

    @Value("${security.token.type:null}")
    private String tokenTypeName;

    @Value("${security.token.server.address:}")
    private String[] address = new String[0];

    @Value("${security.token.server.path:#{null}}")
    private String path;

    @Value("${security.token.operationtimeout:3000}")
    private long operationTimeout;

    @Value("${security.token.ttl:300000}")
    private long ttl;

    @Value("${security.token.maxretrycount:3}")
    private int maxRetryCount;

    public String getTokenTypeName() {
        return tokenTypeName;
    }

    public String[] getAddress() {
        return address;
    }

    public String getPath() {
        return path;
    }

    public long getOperationTimeout() {
        return operationTimeout;
    }

    public long getTtl() {
        return ttl;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenConfig{");
        sb.append("tokenTypeName='").append(tokenTypeName).append('\'');
        sb.append(", address=").append(Arrays.toString(address));
        sb.append(", path='").append(path).append('\'');
        sb.append(", operationTimeout=").append(operationTimeout);
        sb.append(", ttl=").append(ttl);
        sb.append(", maxRetryCount=").append(maxRetryCount);
        sb.append('}');
        return sb.toString();
    }

}
