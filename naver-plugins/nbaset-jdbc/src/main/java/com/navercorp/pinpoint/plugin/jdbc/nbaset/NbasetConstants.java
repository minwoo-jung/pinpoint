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
package com.navercorp.pinpoint.plugin.jdbc.nbaset;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author Jongho Moon
 *
 */
public final class NbasetConstants {
    private NbasetConstants() {
    }

    public static final String NBASET_SCOPE = "NBASET_SCOPE";
    
    public static final ServiceType NBASET = ServiceTypeFactory.of(2410, "NBASE_T", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType NBASET_EXECUTE_QUERY = ServiceTypeFactory.of(2411, "NBASE_T_EXECUTE_QUERY", "NBASE_T", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType NBASET_INTERNAL_METHOD = ServiceTypeFactory.of(2412, "NBASE_T_INTERNAL_METHOD", "NBASE_T");
}
