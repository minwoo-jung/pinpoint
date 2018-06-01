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
import com.navercorp.pinpoint.profiler.context.service.TokenService;
import com.navercorp.pinpoint.profiler.context.service.TcpTokenService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Taejin Koo
 */
public class TokenServiceProvider implements Provider<TokenService> {

    private final Provider<EnhancedDataSender> enhancedDataSenderProvider;

    @Inject
    public TokenServiceProvider(Provider<EnhancedDataSender> enhancedDataSenderProvider) {
        this.enhancedDataSenderProvider = enhancedDataSenderProvider;
    }

    @Override
    public TokenService get() {
        final EnhancedDataSender enhancedDataSender = this.enhancedDataSenderProvider.get();
        TokenService tokenService = new TcpTokenService(enhancedDataSender);
        return tokenService;
    }

}
