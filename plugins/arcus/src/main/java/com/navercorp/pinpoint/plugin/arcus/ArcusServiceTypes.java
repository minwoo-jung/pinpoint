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
package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.common.AnnotationKeyMatcher.*;
import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.plugin.ServiceTypeProvider;

/**
 * @author Jongho Moon
 *
 */
public class ArcusServiceTypes implements ServiceTypeProvider {
    public static final ServiceType ARCUS = ServiceType.of(8100, "ARCUS", FAST_SCHEMA, ARGS_MATCHER, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_FUTURE_GET = ServiceType.of(8101, "ARCUS_FUTURE_GET", "ARCUS", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_EHCACHE_FUTURE_GET = ServiceType.of(8102, "ARCUS_EHCACHE_FUTURE_GET", "ARCUS-EHCACHE", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.common.plugin.ServiceTypeProvider#getServiceTypes()
     */
    @Override
    public ServiceType[] getServiceTypes() {
        return new ServiceType[] { ARCUS, ARCUS_FUTURE_GET, ARCUS_EHCACHE_FUTURE_GET };
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.common.plugin.ServiceTypeProvider#getAnnotationKeys()
     */
    @Override
    public AnnotationKey[] getAnnotationKeys() {
        return new AnnotationKey[] { };
    }
}
