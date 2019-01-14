/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.plugin.lucy.net.interceptor.DefaultInvocationFutureMethodInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.EncodeMessageInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.InvokeMethodInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.NimmInvokerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.CreateConnectorInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InitializeConnectorInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.MakeMessageInterceptor;
import com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.OldVersionConnectorConstructorInterceptor;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class InterceptorConstants {
    private InterceptorConstants() {
    }


    // NPC
    public static final Class<?> METADATA_NPC_SERVER_ADDRESS = NpcServerAddressAccessor.class;
    public static final Class<?> NIMM_ADDRESS_ACCESSOR = NimmAddressAccessor.class;

    // NPC Interceptor
    public static final Class<? extends Interceptor> NPC_CONSTRUCTOR_INTERCEPTOR = ConnectorConstructorInterceptor.class;
    public static final Class<? extends Interceptor> NPC_OLD_CONSTRUCTOR_INTERCEPTOR = OldVersionConnectorConstructorInterceptor.class;
    public static final Class<? extends Interceptor> NPC_INVOKE_INTERCEPTOR = InvokeInterceptor.class;
    public static final Class<? extends Interceptor> NPC_CREATE_CONNECTOR_INTERCEPTOR = CreateConnectorInterceptor.class;
    public static final Class<? extends Interceptor> NPC_INIT_CONNECTOR_INTERCEPTOR = InitializeConnectorInterceptor.class;

    // NIMM Interceptor
    public static final Class<? extends Interceptor> NIMM_CONSTRUCTOR_INTERCEPTOR = NimmInvokerConstructorInterceptor.class;
    public static final Class<? extends Interceptor> NIMM_INVOKE_INTERCEPTOR = InvokeMethodInterceptor.class;
    public static final Class<? extends Interceptor> NIMM_ENCODE_MESSAGE_INTERCEPTOR = EncodeMessageInterceptor.class;


    // NET Common Interceptor
    public static final Class<? extends Interceptor> NET_INVOCATION_FUTURE_INTERCEPTOR = DefaultInvocationFutureMethodInterceptor.class;
    public static final Class<? extends Interceptor> NET_MAKE_MESSAGE_INTERCEPTOR = MakeMessageInterceptor.class;

    public static final Class<? extends Interceptor> BASIC_INTERCEPTOR = BasicMethodInterceptor.class;
}
