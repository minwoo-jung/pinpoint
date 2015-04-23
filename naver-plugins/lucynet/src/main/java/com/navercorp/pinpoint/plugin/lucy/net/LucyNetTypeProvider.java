package com.navercorp.pinpoint.plugin.lucy.net;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

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

/**
 * @author Jongho Moon
 *
 */
public class LucyNetTypeProvider implements TraceMetadataProvider, LucyNetConstants {

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.common.plugin.TraceMetadataProvider#setup(com.navercorp.pinpoint.common.plugin.TraceMetadataSetupContext)
     */
    @Override
    public void setup(TraceMetadataSetupContext context) {
//        context.addType(NPC_CLIENT);
//        context.addAnnotationKey(NPC_CONNECT_OPTION);
//        context.addAnnotationKey(NPC_PARAM);
//        context.addAnnotationKey(NPC_URL);
//
//        context.addType(NIMM_CLIENT);
//        context.addAnnotationKey(NIMM_OBJECT_NAME);
//        context.addAnnotationKey(NIMM_METHOD_NAME);
//        context.addAnnotationKey(NIMM_PARAM);
    }
}
