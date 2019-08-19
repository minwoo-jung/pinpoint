/*
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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import java.nio.charset.Charset;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

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



}
