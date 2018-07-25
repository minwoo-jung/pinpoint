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

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class SslHandlerProvider {

    private final CommonLogger logger;

    private static final String PROTOCOL = "TLS";
    static final String DEFAULT_KEYSTORE = "JKS";

    private static final List<String> DEFAULT_SUPPORT_CIPHER_SUITE;
    private static final List<String> DEFAULT_SUPPORT_PROTOCOLS;
    static {
        List<String> cipherSuite = new ArrayList<String>();

        // tlsV1, tlsV1.1
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");

        // tlsV1.2
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA256");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA256");
        cipherSuite.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");

        DEFAULT_SUPPORT_CIPHER_SUITE = Collections.unmodifiableList(cipherSuite);


        List<String> protocols = new ArrayList<String>();
        protocols.add("TLSv1");
        protocols.add("TLSv1.1");
        protocols.add("TLSv1.2");

        DEFAULT_SUPPORT_PROTOCOLS = Collections.unmodifiableList(protocols);
    }

    public SslHandlerProvider() {
        this.logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(SslHandlerProvider.class.getName());
    }

    public SslHandler createSslHandler(SslConfig.Client config) {
        logger.info("SslConfig:" + config);
        try {
            SslHandlerFactory sslHandlerFactory = new ClientSslHandlerFactory(config);
            return createSslHandler(sslHandlerFactory);
        } catch (SSLException e) {
            logger.warn("failed to create sslHandler", e);
        }
        return null;
    }

    public SslHandler createSslHandler(SslConfig.Server config) {
        logger.info("SslConfig:" + config);
        try {
            SslHandlerFactory sslHandlerFactory = new ServerSslHandlerFactory(config);
            return createSslHandler(sslHandlerFactory);
        } catch (SSLException e) {
            logger.warn("failed to create sslHandler", e);
        }
        return null;
    }

    private SslHandler createSslHandler(SslHandlerFactory sslHandlerFactory) throws SSLException {
        return sslHandlerFactory.createHandler(this);
    }

    String getProtocol() {
        return PROTOCOL;
    }

    SslProvider getSslProvider(String value) throws SSLException {
        if (StringUtils.isEmpty(value)) {
            return SslProvider.JDK;
        }

        if (DEFAULT_KEYSTORE.equalsIgnoreCase(value)) {
            return SslProvider.JDK;
        }

        for (SslProvider sslProvider : SslProvider.values()) {
            if (sslProvider.name().equalsIgnoreCase(value)) {
                return sslProvider;
            }
        }

        throw new SSLException("can't find SslProvider. value:" + value);
    }

    List<String> getSupportCipherSuite() {
        return DEFAULT_SUPPORT_CIPHER_SUITE;
    }

    List<String> getSupportProtocolList() {
        return DEFAULT_SUPPORT_PROTOCOLS;
    }

    SSLEngine createEngine(SSLContext sslContext, boolean clientMode) {
        Assert.requireNonNull(sslContext, "sslContext must not be null");

        SSLEngine engine = sslContext.createSSLEngine();
        List<String> supportedCipherSuites = new ArrayList<String>(Arrays.asList(engine.getSupportedCipherSuites()));
        supportedCipherSuites.retainAll(getSupportCipherSuite());
        String[] supportCipherSuiteArray = supportedCipherSuites.toArray(new String[0]);

        List<String> supportProtocols = new ArrayList<String>(Arrays.asList(engine.getSupportedProtocols()));
        supportProtocols.retainAll(getSupportProtocolList());
        String[] supportProtocolArray = supportProtocols.toArray(new String[0]);

        if (logger.isDebugEnabled()) {
            logger.debug("support cipher suites : " + Arrays.asList(supportCipherSuiteArray));
            logger.debug("support protocols : " + Arrays.asList(supportProtocolArray));
        }

        Assert.isTrue(ArrayUtils.hasLength(supportCipherSuiteArray), "supportCipherSuite must not be empty");
        Assert.isTrue(ArrayUtils.hasLength(supportProtocolArray), "supportProtocol must not be empty");

        engine.setEnabledCipherSuites(supportCipherSuiteArray);
        engine.setEnabledProtocols(supportProtocolArray);
        engine.setUseClientMode(clientMode);
        return engine;
    }

}
