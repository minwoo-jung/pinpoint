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

package com.navercorp.pinpoint.grpc.security.server;

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.Attributes;

/**
 * @author Taejin Koo
 */
public class GrpcSecurityAttribute {

    private static final Attributes.Key<AuthContext> AUTH_CONTEXT_ATTRIBUTE = Attributes.Key.create("pinpointAuthContext");

    public static AuthContext getAuthContext(Attributes attributes) {
        Assert.requireNonNull(attributes, "attributes must not be null");
        return attributes.get(AUTH_CONTEXT_ATTRIBUTE);
    }

    public static Attributes setAuthContext(Attributes attributes) {
        Attributes.Builder attributesBuilder = attributes.toBuilder();
        attributesBuilder.set(AUTH_CONTEXT_ATTRIBUTE, new DefaultAuthStateContext());
        return attributesBuilder.build();
    }

}
