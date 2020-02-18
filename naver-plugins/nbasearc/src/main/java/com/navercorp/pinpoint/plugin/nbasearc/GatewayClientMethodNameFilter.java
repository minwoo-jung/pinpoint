/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class GatewayClientMethodNameFilter implements MethodFilter {

    private final Set<String> excludeMethodNames = new HashSet<String>(16);

    public GatewayClientMethodNameFilter() {
        // object methods.
        this.excludeMethodNames.addAll(Arrays.asList("clone", "equals", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"));
        // unnecessary methods.
        this.excludeMethodNames.addAll(Arrays.asList("setServer", "ping", "destroy", "close", "sync", "getGateway", "getClient", "pipeline", "pipelineCallback", "executeBackgroundPool", "execute", "executeCallback", "toExecuteInfo", "quit", "disconnect", "passivate", "isConnected", "getTupledSet", "getConnection", "setKeyspace", "getKeyspace", "connectInfo"));
    }

    @Override
    public boolean accept(final InstrumentMethod method) {
        if (method != null) {
            final int modifiers = method.getModifiers();
            // only public.
            if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
                return false;
            }

            final String name = method.getName();
            // skip pinpoint and object methods.
            if (!name.startsWith("_$PINPOINT$_") && !this.excludeMethodNames.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
