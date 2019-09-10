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
class DefaultSecurityContext implements SecurityContext {

    private final AtomicReference<SecurityState> currentState = new AtomicReference<SecurityState>(SecurityState.BEFORE);

    boolean toSuccess() {
        return currentState.compareAndSet(SecurityState.BEFORE, SecurityState.SUCCESS);
    }

    boolean toFail() {
        return currentState.compareAndSet(SecurityState.BEFORE, SecurityState.FAIL);
    }

    boolean toExpired() {
        return currentState.compareAndSet(SecurityState.SUCCESS, SecurityState.EXPIRED);
    }

    @Override
    public SecurityState getState() {
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
