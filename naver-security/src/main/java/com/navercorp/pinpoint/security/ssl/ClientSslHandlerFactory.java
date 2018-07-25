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
import org.jboss.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

/**
 * @author Taejin Koo
 */
public class ClientSslHandlerFactory implements SslHandlerFactory {

    private final SslConfig.Client config;

    public ClientSslHandlerFactory(SslConfig.Client config) {
        this.config = Assert.requireNonNull(config, "config must not be null");
    }

    @Override
    public SslHandler createHandler(SslHandlerProvider sslHandlerProvider) throws SSLException {
        try {
            String protocol = sslHandlerProvider.getProtocol();
            SSLContext sslContext = SSLContext.getInstance(protocol);

            TrustManagerFactory trustManagerFactory = getTrustManagerFactory();
            trustManagerFactory.init((KeyStore) null);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            SSLEngine engine = sslHandlerProvider.createEngine(sslContext, true);
            return new SslHandler(engine);
        } catch (Exception e) {
            throw new SSLException(e.getMessage(), e);
        }
    }

    private TrustManagerFactory getTrustManagerFactory() throws NoSuchAlgorithmException {
        if (config.isInsecureEnable()) {
            return InsecureTrustManagerFactory.INSTANCE;
        } else {
            String algorithm = config.getTrustManagerAlgorithm();
            if (StringUtils.isEmpty(algorithm)) {
                String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                return TrustManagerFactory.getInstance(defaultAlgorithm);
            } else {
                return TrustManagerFactory.getInstance(algorithm);
            }
        }
    }

}
