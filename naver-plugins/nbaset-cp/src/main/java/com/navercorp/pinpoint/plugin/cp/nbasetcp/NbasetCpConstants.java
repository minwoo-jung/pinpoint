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

package com.navercorp.pinpoint.plugin.cp.nbasetcp;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Taejin Koo
 */
public class NbasetCpConstants {

    private NbasetCpConstants() {
    }

    public static final String SCOPE = "NBASET_CP_SCOPE";

    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(2415, "NBASE_T_CP");

    public static final String CP_URL = "NBaseT use only 1 connection pool. (Connection pool is shared by everyone)";
    public static final String CP_NAME = "NBASE_T_CP";


    // field in com.nhncorp.nbase_t.krpc.core.ConnectionPool
    public static final String FIELD_NUM_USED_SOCKET = "numUsedSockets";
    public static final String FIELD_MAX_POOL_SIZE = "maxConnPoolSize";


}
