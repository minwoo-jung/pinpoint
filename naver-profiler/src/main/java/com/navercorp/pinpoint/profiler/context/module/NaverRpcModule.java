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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.provider.HeaderTBaseSerializerProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.CommandDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.NaverConnectionFactoryProviderProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanStatClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.StatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public class NaverRpcModule extends PrivateModule {

    public NaverRpcModule() {
    }

    @Override
    protected void configure() {
        bind(CommandDispatcher.class).toProvider(CommandDispatcherProvider.class).in(Scopes.SINGLETON);

        // for enable ssl
        bind(ConnectionFactoryProvider.class).toProvider(NaverConnectionFactoryProviderProvider.class).in(Scopes.SINGLETON);

        Key<PinpointClientFactory> pinpointClientFactory = Key.get(PinpointClientFactory.class, DefaultClientFactory.class);
        bind(pinpointClientFactory).toProvider(PinpointClientFactoryProvider.class).in(Scopes.SINGLETON);
        expose(pinpointClientFactory);

        bind(HeaderTBaseSerializer.class).toProvider(HeaderTBaseSerializerProvider.class).in(Scopes.SINGLETON);

        // for enable tokenService
        // bind(HeaderTBaseSerializer.class).toProvider(TokenHeaderTBaseSerializerProvider.class).in(Scopes.SINGLETON);

        bind(EnhancedDataSender.class).toProvider(TcpDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(EnhancedDataSender.class);

        // for enable tokenService
        // bind(TokenService.class).toProvider(TokenServiceProvider.class).in(Scopes.SINGLETON);
        // 1. set tokenService into DataSender
        // bind(spanDataSender).toProvider(SpanDataSenderProvider.class).in(Scopes.SINGLETON);
        // 2. set tokenService into ChanelHandler in ClientFactory
        // bind(pinpointStatClientFactory).toProvider(TokenEnableSpanStatClientFactoryProvider.class).in(Scopes.SINGLETON);

        Key<PinpointClientFactory> pinpointStatClientFactory = Key.get(PinpointClientFactory.class, SpanStatClientFactory.class);
        bind(pinpointStatClientFactory).toProvider(SpanStatClientFactoryProvider.class).in(Scopes.SINGLETON);
        expose(pinpointStatClientFactory);

        Key<DataSender> spanDataSender = Key.get(DataSender.class, SpanDataSender.class);
        bind(spanDataSender).toProvider(SpanDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(spanDataSender);

        Key<DataSender> statDataSender = Key.get(DataSender.class, StatDataSender.class);
        bind(DataSender.class).annotatedWith(StatDataSender.class)
                .toProvider(StatDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(statDataSender);
    }

}
