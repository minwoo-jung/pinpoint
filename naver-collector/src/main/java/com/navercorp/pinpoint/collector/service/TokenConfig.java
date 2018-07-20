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

package com.navercorp.pinpoint.collector.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Taejin Koo
 */
@Component
@Profile("tokenAuthentication")
public class TokenConfig {

    @Value("#{pinpoint_collector_properties['collector.receiver.token.zookeeper.address'] ?: null}")
    private String address;

    @Value("#{pinpoint_collector_properties['collector.receiver.token.zookeeper.path'] ?: null}")
    private String path;

    @Value("#{pinpoint_collector_properties['collector.receiver.token.zookeeper.sessiontimeout'] ?: 30000}")
    private int sessionTimeout;

    @Value("#{pinpoint_collector_properties['collector.receiver.token.operation.retry.interval'] ?: 5000}")
    private long operationRetryInterval;

    @Value("#{pinpoint_collector_properties['collector.receiver.token.ttl'] ?: 300000}")
    private long ttl;

    @Value("#{pinpoint_collector_properties['collector.receiver.token.maxretrycount'] ?: 3}")
    private int maxRetryCount;

    public String getAddress() {
        return address;
    }

    public String getPath() {
        return path;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public long getOperationRetryInterval() {
        return operationRetryInterval;
    }

    public long getTtl() {
        return ttl;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public String toString() {
        return "TokenConfig{" +
                "address='" + address + '\'' +
                ", path='" + path + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", operationRetryInterval=" + operationRetryInterval +
                ", ttl=" + ttl +
                ", maxRetryCount=" + maxRetryCount +
                '}';
    }

}
