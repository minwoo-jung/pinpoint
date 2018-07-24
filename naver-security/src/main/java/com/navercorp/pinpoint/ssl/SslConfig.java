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

package com.navercorp.pinpoint.ssl;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class SslConfig {

    public static final String KEY_SSL_ENABLE = "ssl.enable";
    public static final String KEY_SSL_PROVIDER_TYPE = "ssl.provider.type";

    // for client
    public static final String KEY_SSL_TRUSTMANAGER_ALGORITHM = "ssl.trustmanager.algorithm";
    // for test
    public static final String KEY_SSL_TRUSTMANAGER_INSECURE = "ssl.trustmanager.algorithm.inseucre";


    // for server
    public static final String KEY_SSL_STORE_FILE_PATH = "ssl.store.file.path";
    public static final String KEY_SSL_STORE_PASSWORD = "ssl.store.password";
    public static final String KEY_SSL_KEYMANAGER_ALGORITHM = "ssl.keymanager.algorithm";
    public static final String KEY_SSL_KEYMANAGER_PASSWORD = "ssl.keymanager.password";


    private static final String DEFAULT_ENABLE = "false";
    private static final String DEFAULT_ALGORITHM = "SunX509";

    private static final String DEFAULT_KEY_PROVIDER_TYPE = "JDK";

    public static Client createClientSslConfig(Properties properties) {
        Assert.requireNonNull(properties, "properties must not be null");

        boolean enable = Boolean.valueOf(properties.getProperty(KEY_SSL_ENABLE, DEFAULT_ENABLE));
        String keyProviderType = properties.getProperty(KEY_SSL_PROVIDER_TYPE, DEFAULT_KEY_PROVIDER_TYPE);
        String trustManagerAlgorithm = properties.getProperty(KEY_SSL_TRUSTMANAGER_ALGORITHM, "");
        boolean insecureMode = Boolean.valueOf(properties.getProperty(KEY_SSL_TRUSTMANAGER_INSECURE, DEFAULT_ENABLE));

        return new Client(enable, String.valueOf(keyProviderType), trustManagerAlgorithm, insecureMode);
    }

    public static Server createServerSslConfig(Properties properties) {
        Assert.requireNonNull(properties, "properties must not be null");

        boolean enable = Boolean.valueOf(properties.getProperty(KEY_SSL_ENABLE, DEFAULT_ENABLE));

        String keyProviderType = properties.getProperty(KEY_SSL_PROVIDER_TYPE, DEFAULT_KEY_PROVIDER_TYPE);
        String keyStoreFilePath = properties.getProperty(KEY_SSL_STORE_FILE_PATH, "");
        String keyStorePassword = properties.getProperty(KEY_SSL_STORE_PASSWORD, "");
        String keyManagerFactoryAlgorithm = properties.getProperty(KEY_SSL_KEYMANAGER_ALGORITHM, "");
        String keyManagerFactoryPassword = properties.getProperty(KEY_SSL_KEYMANAGER_PASSWORD, keyStorePassword);

        return new Server(enable, String.valueOf(keyProviderType), keyStoreFilePath, keyStorePassword, keyManagerFactoryAlgorithm, keyManagerFactoryPassword);
    }

    public static class Client {

        private final boolean enable;
        private final String keyProviderType;
        private final String trustManagerAlgorithm;
        private final boolean insecureEnable;

        private Client(boolean enable, String keyProviderType, String trustManagerAlgorithm, boolean insecureEnable) {
            this.enable = enable;
            this.keyProviderType = Assert.requireNonNull(keyProviderType, "keyProviderType must not be null");
            this.trustManagerAlgorithm = Assert.requireNonNull(trustManagerAlgorithm, "trustManagerAlgorithm must not be null");
            this.insecureEnable = insecureEnable;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getKeyProviderType() {
            return keyProviderType;
        }

        public String getTrustManagerAlgorithm() {
            return trustManagerAlgorithm;
        }

        public boolean isInsecureEnable() {
            return insecureEnable;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Client{");
            sb.append("enable=").append(enable);
            sb.append(", keyProviderType='").append(keyProviderType).append('\'');
            sb.append(", trustManagerAlgorithm='").append(trustManagerAlgorithm).append('\'');
            sb.append(", insecureEnable=").append(insecureEnable);
            sb.append('}');
            return sb.toString();
        }

    }

    public static class Server {

        private final boolean enable;
        private final String keyProviderType;
        private final String keyStoreFilePath;
        private final String keyStorePassword;
        private final String keyManagerFactoryAlgorithm;
        private final String keyManagerFactoryPassword;

        public Server(boolean enable, String keyProviderType, String keyStoreFilePath, String keyStorePassword, String keyManagerFactoryAlgorithm, String keyManagerFactoryPassword) {
            this.enable = enable;

            this.keyProviderType = Assert.requireNonNull(keyProviderType, "keyProviderType must not be null");

            Assert.isTrue(StringUtils.hasLength(keyStoreFilePath), "keyStoreFilePath is empty");
            this.keyStoreFilePath = keyStoreFilePath;
            this.keyStorePassword = Assert.requireNonNull(keyStorePassword, "keyStorePassword must not be null");

            this.keyManagerFactoryAlgorithm = Assert.requireNonNull(keyManagerFactoryAlgorithm, "keyManagerFactoryAlgorithm must not be null");
            this.keyManagerFactoryPassword = Assert.requireNonNull(keyManagerFactoryPassword, "keyManagerFactoryPassword must not be null");
        }

        public boolean isEnable() {
            return enable;
        }

        public String getKeyProviderType() {
            return keyProviderType;
        }

        public String getKeyStoreFilePath() {
            return keyStoreFilePath;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public String getKeyManagerFactoryAlgorithm() {
            return keyManagerFactoryAlgorithm;
        }

        public String getKeyManagerFactoryPassword() {
            return keyManagerFactoryPassword;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Server{");
            sb.append("enable=").append(enable);
            sb.append(", keyProviderType='").append(keyProviderType).append('\'');
            sb.append(", keyStoreFilePath='").append(keyStoreFilePath).append('\'');
            sb.append(", keyStorePassword='").append(keyStorePassword).append('\'');
            sb.append(", keyManagerFactoryAlgorithm='").append(keyManagerFactoryAlgorithm).append('\'');
            sb.append(", keyManagerFactoryPassword='").append(keyManagerFactoryPassword).append('\'');
            sb.append('}');
            return sb.toString();
        }

    }

}
