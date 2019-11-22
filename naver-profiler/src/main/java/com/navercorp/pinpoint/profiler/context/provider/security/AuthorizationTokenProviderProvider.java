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

package com.navercorp.pinpoint.profiler.context.provider.security;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.security.client.AuthorizationTokenProvider;
import com.navercorp.pinpoint.profiler.context.module.AgentDataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class AuthorizationTokenProviderProvider implements Provider<AuthorizationTokenProvider> {

    private final Provider<EnhancedDataSender<Object>> agentDataSenderProvider;

    @Inject
    public AuthorizationTokenProviderProvider(@AgentDataSender Provider<EnhancedDataSender<Object>> agentDataSenderProvider) {
        this.agentDataSenderProvider = Assert.requireNonNull(agentDataSenderProvider, "agentDataSenderProvider");
    }

    @Override
    public AuthorizationTokenProvider get() {
        EnhancedDataSender<Object> objectEnhancedDataSender = agentDataSenderProvider.get();

        if (objectEnhancedDataSender instanceof AuthorizationTokenProvider) {
            return (AuthorizationTokenProvider) objectEnhancedDataSender;
        } else {
            return null;
        }
    }

}
