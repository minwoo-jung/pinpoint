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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.TokenEnableConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;

/**
 * @author Taejin Koo
 */
public class TokenEnableSpanStatClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ProfilerConfig profilerConfig;
    private final ConnectionFactoryProvider connectionFactoryProvider;

    @Inject
    public TokenEnableSpanStatClientFactoryProvider(ProfilerConfig profilerConfig,
                                                    @TokenEnableConnectionFactoryProvider ConnectionFactoryProvider connectionFactoryProvider) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.connectionFactoryProvider = Assert.requireNonNull(connectionFactoryProvider, "connectionFactoryProvider must not be null");
    }

    public PinpointClientFactory get() {
        if (!"TCP".equalsIgnoreCase(profilerConfig.getSpanDataSenderTransportType()) || !"TCP".equalsIgnoreCase(profilerConfig.getStatDataSenderTransportType())) {
            throw new IllegalArgumentException("Must have a tcp connection to enable token security.");
        }

        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory(1, 2, connectionFactoryProvider);
        pinpointClientFactory.setWriteTimeoutMillis(1000 * 3);
        pinpointClientFactory.setRequestTimeoutMillis(1000 * 5);
        return pinpointClientFactory;
    }

}
