/**
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import java.nio.charset.Charset;

/**
 * @author Jongho Moon
 *
 */
public final class LucyNetConstants {
    private LucyNetConstants() {
    }

    // Common
    public static final Charset UTF_8_CHARSET = Charsets.UTF_8;
    public static final String UNKOWN_ADDRESS = "Unknown Address";

    // NPC
    public static final String METADATA_NPC_SERVER_ADDRESS = "com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor";
    public static final String NIMM_ADDRESS_ACCESSOR = "com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor";

    public static final ServiceType NPC_CLIENT = ServiceTypeFactory.of(9060, "NPC_CLIENT", RECORD_STATISTICS);
    public static final ServiceType NPC_CLIENT_INTERNAL = ServiceTypeFactory.of(9061, "NPC_CLIENT_INTERNAL", "NPC_CLIENT");

    public static final AnnotationKey NPC_URL = AnnotationKeyFactory.of(60, "npc.url", VIEW_IN_RECORD_SET);
    public static final AnnotationKey NPC_PARAM = AnnotationKeyFactory.of(61, "npc.param", VIEW_IN_RECORD_SET);
    public static final AnnotationKey NPC_CONNECT_OPTION = AnnotationKeyFactory.of(62, "npc.connect.options", VIEW_IN_RECORD_SET);

    // NIMM
    public static final ServiceType NIMM_CLIENT = ServiceTypeFactory.of(9070, "NIMM_CLIENT", RECORD_STATISTICS);

    public static final AnnotationKey NIMM_OBJECT_NAME = AnnotationKeyFactory.of(70, "nimm.objectName", VIEW_IN_RECORD_SET);
    public static final AnnotationKey NIMM_METHOD_NAME = AnnotationKeyFactory.of(71, "nimm.methodName", VIEW_IN_RECORD_SET);
    public static final AnnotationKey NIMM_PARAM = AnnotationKeyFactory.of(72, "nimm.param", VIEW_IN_RECORD_SET);
    public static final AnnotationKey NIMM_URL = AnnotationKeyFactory.of(73, "nimm.url");

    public static final String NIMM_INVOKER_METHOD_SCOPE = "NimmInvokerMethodScope";

    // NPC Interceptor
    public static final String NPC_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor";
    public static final String NPC_OLD_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.OldVersionConnectorConstructorInterceptor";
    public static final String NPC_INVOKE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor";
    public static final String NPC_CREATE_CONNECTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.CreateConnectorInterceptor";
    public static final String NPC_INIT_CONNECTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InitializeConnectorInterceptor";

    // NIMM Interceptor
    public static final String NIMM_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.NimmInvokerConstructorInterceptor";
    public static final String NIMM_INVOKE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.InvokeMethodInterceptor";
    public static final String NIMM_ENCODE_MESSAGE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.EncodeMesssageInterceptor";


    // NET Common Interceptor
    public static final String NET_INVOCATION_FUTURE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.interceptor.DefaultInvocationFutureMethodInterceptor";
    public static final String NET_MAKE_MESSAGE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.MakeMessageInterceptor";

    public static final String BASIC_INTERCEPTOR = "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor";

}
