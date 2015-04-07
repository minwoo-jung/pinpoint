/*
 * Copyright 2014 NAVER Corp.
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

import static com.navercorp.pinpoint.common.HistogramSchema.FAST_SCHEMA;
import static com.navercorp.pinpoint.common.HistogramSchema.NORMAL_SCHEMA;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.TERMINAL;

import com.navercorp.pinpoint.common.ServiceType;

/**
 * 
 * @author jaehong.kim
 *
 */
public interface NbaseArcConstants {
    public static final ServiceType NBASE_ARC = ServiceType.of(8250, "NBASE_ARC", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType NBASE_ARC_INTERNAL = ServiceType.of(8251, "NBASE_ARC_INTERNAL", NORMAL_SCHEMA);

    public static final String METADATA_END_POINT = "endPoint";
    public static final String METADATA_DESTINATION_ID = "destinationId";
    public static final String NBASE_ARC_SCOPE = "nBaseArcScope";
}
