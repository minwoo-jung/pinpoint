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

package com.navercorp.pinpoint.grpc.security;

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.Metadata;

/**
 * @author Taejin Koo
 */
public class GrpcSecurityMetadata {

    // for authentication
    private static final Metadata.Key<String> AUTH_KEY = Metadata.Key.of("pinpointAuthenticationKeyMetadata", Metadata.ASCII_STRING_MARSHALLER);

    // for authorization
    private static final Metadata.Key<String> AUTH_TOKEN = Metadata.Key.of("pinpointAuthorizationTokenMetadata", Metadata.ASCII_STRING_MARSHALLER);

    public static String getAuthKey(Metadata metadata) {
        Assert.requireNonNull(metadata, "metadata must not be null");
        return metadata.get(AUTH_KEY);
    }

    public static void setAuthKey(Metadata metadata, String authKey) {
        metadata.put(AUTH_KEY, authKey);
    }

    public static String getAuthToken(Metadata metadata) {
        Assert.requireNonNull(metadata, "metadata must not be null");
        return metadata.get(AUTH_TOKEN);
    }

    public static void setAuthToken(Metadata metadata, String authToken) {
        metadata.put(AUTH_TOKEN, authToken);
    }

}
