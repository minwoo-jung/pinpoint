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

import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author Jongho Moon
 *
 */
public interface LucyNetConstants {
    // NPC
    public static final String METADATA_NPC_SERVER_ADDRESS = "npcServerAddress";
    
    public static final ServiceType NPC_CLIENT = ServiceType.of(9060, "NPC_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    
    public static final AnnotationKey NPC_URL = new AnnotationKey(60, "npc.url");
    public static final AnnotationKey NPC_PARAM = new AnnotationKey(61, "npc.param");
    public static final AnnotationKey NPC_CONNECT_OPTION = new AnnotationKey(62, "npc.connect.options");

    // NIMM
    public static final String METADATA_NIMM_ADDRESS = "nimmAddress";

    public static final ServiceType NIMM_CLIENT = ServiceType.of(9070, "NIMM_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    
    public static final AnnotationKey NIMM_OBJECT_NAME = new AnnotationKey(70, "nimm.objectName");
    public static final AnnotationKey NIMM_METHOD_NAME = new AnnotationKey(71, "nimm.methodName");
    public static final AnnotationKey NIMM_PARAM = new AnnotationKey(72, "nimm.param");
    
    // TODO 사용되는 곳이 없음. 필요 없으면 삭제하자 
    public static final AnnotationKey NIMM_CONNECT_OPTION = new AnnotationKey(73, "nimm.connect.options");
}
