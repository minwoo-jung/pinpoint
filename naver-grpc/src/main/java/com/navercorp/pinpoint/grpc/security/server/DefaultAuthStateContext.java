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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class DefaultAuthStateContext implements AuthContext {

    private final AtomicReference<AuthState> currentState = new AtomicReference<AuthState>(AuthState.NONE);

    public boolean changeState(AuthState nextState) {
        if (nextState == AuthState.SUCCESS) {
            return currentState.compareAndSet(AuthState.NONE, nextState);
        } else if (nextState == AuthState.FAIL) {
            return currentState.compareAndSet(AuthState.NONE, nextState);
        } else if (nextState == AuthState.EXPIRED) {
            return currentState.compareAndSet(AuthState.SUCCESS, nextState);
        }

        throw new IllegalArgumentException("do not support nextState:" + nextState);
    }

    @Override
    public AuthState getState() {
        return currentState.get();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultSecurityContext{");
        sb.append("state=").append(getState());
        sb.append('}');
        return sb.toString();
    }

}
