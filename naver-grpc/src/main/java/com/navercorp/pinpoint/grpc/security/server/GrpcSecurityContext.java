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

import com.navercorp.pinpoint.grpc.server.TransportMetadata;

import io.grpc.Context;

/**
 * @author Taejin Koo
 */
public class GrpcSecurityContext {

    private static final Context.Key<AuthKeyHolder> AUTH_KEY_CONTEXT = Context.key("authKeyHolder");

    private static final Context.Key<AuthTokenHolder> AUTH_TOKEN_CONTEXT = Context.key("authTokenHolder");

    private static final Context.Key<AuthContext> AUTH_CONTEXT = Context.key("authContext");

    public static AuthKeyHolder getAuthKeyHolder() {
        final Context current = Context.current();
        return AUTH_KEY_CONTEXT.get(current);
    }

    public static Context setAuthKeyHolder(String authKey) {
        final Context current = Context.current();
        return current.withValue(AUTH_KEY_CONTEXT, new AuthKeyHolder(authKey));
    }

    public static AuthTokenHolder getAuthTokenHolder() {
        final Context current = Context.current();
        return AUTH_TOKEN_CONTEXT.get(current);
    }

    public static Context setAuthTokenHolder(String authToken) {
        final Context current = Context.current();
        return current.withValue(AUTH_TOKEN_CONTEXT, new AuthTokenHolder(authToken));
    }

    public static AuthContext getAuthContext() {
        final Context current = Context.current();
        return AUTH_CONTEXT.get(current);
    }

    public static Context setAuthContext(AuthContext authContext) {
        final Context current = Context.current();
        return current.withValue(AUTH_CONTEXT, authContext);
    }


}
