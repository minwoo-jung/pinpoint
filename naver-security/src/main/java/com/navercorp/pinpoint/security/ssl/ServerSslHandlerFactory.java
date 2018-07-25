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

package com.navercorp.pinpoint.security.ssl;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

/**
 * @author Taejin Koo
 */
public class ServerSslHandlerFactory implements SslHandlerFactory {

    private final SslConfig.Server config;

    public ServerSslHandlerFactory(SslConfig.Server config) {
        this.config = Assert.requireNonNull(config, "config must not be null");
    }

    @Override
    public SslHandler createHandler(SslHandlerProvider sslHandlerProvider) throws SSLException {
        try {
            String protocol = sslHandlerProvider.getProtocol();
            SSLContext sslContext = SSLContext.getInstance(protocol);

            KeyStore keyStore = createKeyStore(sslHandlerProvider);
            KeyManagerFactory keyManagerFactory = createKeyManagerFactory();

            keyManagerFactory.init(keyStore, config.getKeyManagerFactoryPassword().toCharArray());

            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            SSLEngine engine = sslHandlerProvider.createEngine(sslContext, false);
            return new SslHandler(engine);
        } catch (Exception e) {
            throw new SSLException("can't create SslHandlerFactory. caused:" + e.getMessage(), e);
        }
    }

    private KeyStore createKeyStore(SslHandlerProvider sslHandlerProvider) throws Exception {
        SslProvider sslProvider = sslHandlerProvider.getSslProvider(config.getKeyProviderType());
        if (sslProvider == SslProvider.JDK) {
            KeyStore keyStore = KeyStore.getInstance(sslHandlerProvider.DEFAULT_KEYSTORE);

            InputStream inputStream = null;
            try {
                inputStream = findInputStream(config.getKeyStoreFilePath());
                if (inputStream == null) {
                    throw new SSLException("can't find keyStore file.");
                }
                keyStore.load(inputStream, config.getKeyStorePassword().toCharArray());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            return keyStore;
        } else {
            throw new SSLException("not supported SslProvider:" + sslProvider);
        }
    }

    private InputStream findInputStream(String path) {
        // find order
        // 1. application classLoader -> 2. ServerSslHandlerFactory classLoader -> 3 Absolute path
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (inputStream != null) {
            return inputStream;
        }

        inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream != null) {
            return inputStream;
        }

        try {
            final File file = new File(path);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            // skip
        }

        return null;
    }

    private KeyManagerFactory createKeyManagerFactory() throws NoSuchAlgorithmException {
        String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        if (StringUtils.hasText(config.getKeyManagerFactoryAlgorithm())) {
            keyManagerFactoryAlgorithm = config.getKeyManagerFactoryAlgorithm();
        }

        return KeyManagerFactory.getInstance(keyManagerFactoryAlgorithm);
    }



}
