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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.client.ClientCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.DefaultConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.codec.PacketDecoder;
import com.navercorp.pinpoint.rpc.codec.PacketEncoder;
import com.navercorp.pinpoint.ssl.SslConfig;
import com.navercorp.pinpoint.ssl.SslHandlerProvider;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslHandler;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class NaverConnectionFactoryProviderProvider implements Provider<ConnectionFactoryProvider> {

    private final ProfilerConfig profilerConfig;

    private static final String KEY_SSL_ENABLE = "profiler.collector.tcp.ssl.enable";
    private static final String DEFAULT_ENABLE = "false";

    private static final String KEY_SSL_ALGORITHM = "profiler.collector.tcp.ssl.algorithm";
    private static final String KEY_SSL_ALGORITHM_INSECURE = "profiler.collector.tcp.ssl.algorithm.insecure";

    @Inject
    public NaverConnectionFactoryProviderProvider(ProfilerConfig profilerConfig) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
    }

    @Override
    public ConnectionFactoryProvider get() {
        String enable = profilerConfig.readString(KEY_SSL_ENABLE, DEFAULT_ENABLE);

        if (Boolean.valueOf(enable)) {
            Properties properties = createProperties(profilerConfig);

            final SslConfig.Client sslConfig = SslConfig.createClientSslConfig(properties);

            return new DefaultConnectionFactoryProvider(new PipelineFactory() {
                @Override
                public ChannelPipeline newPipeline() {
                    SslHandlerProvider sslHandlerProvider = new SslHandlerProvider();
                    final SslHandler sslHandler = sslHandlerProvider.createSslHandler(sslConfig);
                    if (sslHandler == null) {
                        return null;
                    }

                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("ssl", sslHandler);
                    pipeline.addLast("encoder", new PacketEncoder());
                    pipeline.addLast("decoder", new PacketDecoder());

                    return pipeline;
                }
            });
        } else {
            return new DefaultConnectionFactoryProvider(new ClientCodecPipelineFactory());
        }
    }

    private Properties createProperties(ProfilerConfig profilerConfig) {
        String enable = profilerConfig.readString(KEY_SSL_ENABLE, DEFAULT_ENABLE);
        String algorithm = profilerConfig.readString(KEY_SSL_ALGORITHM, "");
        String insecureMode = profilerConfig.readString(KEY_SSL_ALGORITHM_INSECURE, DEFAULT_ENABLE);

        Properties properties = new Properties();
        properties.put(SslConfig.KEY_SSL_ENABLE, enable);
        properties.put(SslConfig.KEY_SSL_TRUSTMANAGER_ALGORITHM, algorithm);
        properties.put(SslConfig.KEY_SSL_TRUSTMANAGER_INSECURE, insecureMode);

        return properties;
    }

}

