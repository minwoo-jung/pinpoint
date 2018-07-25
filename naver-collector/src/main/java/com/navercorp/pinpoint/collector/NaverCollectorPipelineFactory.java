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

package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.server.ServerCodecPipelineFactory;
import com.navercorp.pinpoint.security.ssl.SslConfig;
import com.navercorp.pinpoint.security.ssl.SslHandlerProvider;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.ssl.SslHandler;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class NaverCollectorPipelineFactory implements PipelineFactory {

    private static final String KEY_SSL_ENABLE = "collector.receiver.base.ssl.enable";
    private static final String DEFAULT_SSL_ENABLE = "false";

    private static final String KEY_SSL_KEYSTORE_TYPE = "collector.receiver.base.ssl.keystore.type";
    private static final String KEY_SSL_KEYSTORE_PATH = "collector.receiver.base.ssl.keystore.path";
    private static final String KEY_SSL_KEYSTORE_PASSWORD = "collector.receiver.base.ssl.keystore.password";
    private static final String KEY_SSL_KEYMANAGER_ALGORITHM = "collector.receiver.base.ssl.keymanager.algorithm";
    private static final String KEY_SSL_KEYMANAGER_PASSWORD = "collector.receiver.base.ssl.keymanager.password";

    private final ServerCodecPipelineFactory serverCodecPipelineFactory = new ServerCodecPipelineFactory();

    private final SslConfig.Server sslConfig;
    private final SslHandlerProvider sslHandlerProvider;

    public NaverCollectorPipelineFactory(Properties properties) {
        Assert.requireNonNull(properties, "properties must not be null");
        String enable = properties.getProperty(KEY_SSL_ENABLE, DEFAULT_SSL_ENABLE);

        if (Boolean.valueOf(enable)) {
            Properties newProperties = createProperties(properties);

            this.sslConfig = SslConfig.createServerSslConfig(newProperties);
            this.sslHandlerProvider = new SslHandlerProvider();
        } else {
            this.sslConfig = null;
            this.sslHandlerProvider = null;
        }
    }

    private Properties createProperties(Properties properties) {
        String enable = properties.getProperty(KEY_SSL_ENABLE, DEFAULT_SSL_ENABLE);
        String keyStoreType = properties.getProperty(KEY_SSL_KEYSTORE_TYPE, "");

        String keyStorePath = properties.getProperty(KEY_SSL_KEYSTORE_PATH, "");
        String keyStorePassword = properties.getProperty(KEY_SSL_KEYSTORE_PASSWORD, "");
        String keyManagerAlgorithm = properties.getProperty(KEY_SSL_KEYMANAGER_ALGORITHM, "");
        String keyManagerPassword = properties.getProperty(KEY_SSL_KEYMANAGER_PASSWORD, keyStorePassword);

        Properties newProperties = new Properties();
        newProperties.put(SslConfig.KEY_SSL_ENABLE, enable);
        newProperties.put(SslConfig.KEY_SSL_PROVIDER_TYPE, keyStoreType);
        newProperties.put(SslConfig.KEY_SSL_STORE_FILE_PATH, keyStorePath);
        newProperties.put(SslConfig.KEY_SSL_STORE_PASSWORD, keyStorePassword);
        newProperties.put(SslConfig.KEY_SSL_KEYMANAGER_ALGORITHM, keyManagerAlgorithm);
        newProperties.put(SslConfig.KEY_SSL_KEYMANAGER_PASSWORD, keyManagerPassword);
        return newProperties;
    }

    @Override
    public ChannelPipeline newPipeline() {
        ChannelPipeline pipeline = serverCodecPipelineFactory.newPipeline();
        if (sslHandlerProvider != null && sslConfig != null) {
            SslHandler sslHandler = sslHandlerProvider.createSslHandler(sslConfig);
            pipeline.addFirst("ssl", sslHandler);
        }

        return pipeline;
    }

}
