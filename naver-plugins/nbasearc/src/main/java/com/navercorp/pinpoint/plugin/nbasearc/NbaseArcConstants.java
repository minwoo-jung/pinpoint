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

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * 
 * @author jaehong.kim
 *
 */
public final class NbaseArcConstants {
    private NbaseArcConstants() {
    }

    public static final ServiceType NBASE_ARC = ServiceTypeFactory.of(8250, "NBASE_ARC", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType NBASE_ARC_INTERNAL = ServiceTypeFactory.of(8251, "NBASE_ARC_INTERNAL");
    
    public static final String NBASE_ARC_SCOPE = "nBaseArcScope";


}
