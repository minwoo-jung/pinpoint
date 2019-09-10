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

import com.navercorp.pinpoint.grpc.security.server.SecurityContext;

import io.grpc.Attributes;
import io.grpc.Metadata;

/**
 * @author Taejin Koo
 */
public class SecurityConstants {

    public static final Metadata.Key<String> AUTHENTICATION_KEY = Metadata.Key.of("pinpointAuthenticationKey", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> AUTHORIZATION_TOKEN = Metadata.Key.of("pinpointAuthorizationToken", Metadata.ASCII_STRING_MARSHALLER);

    public static final Attributes.Key<SecurityContext> SECURITY_CONTEXT = Attributes.Key.create("pinpointSecurityContext");

}
