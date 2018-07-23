/**
 * Copyright 2018 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jeus;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author jaehong.kim
 */
public final class JeusConstants {
    private JeusConstants() {
    }

    public static final ServiceType JEUS = ServiceTypeFactory.of(1080, "JEUS", RECORD_STATISTICS);
    public static final ServiceType JEUS_METHOD = ServiceTypeFactory.of(1081, "JEUS_METHOD");
    public static final String STATUS_CODE_ACCESSOR = "com.navercorp.pinpoint.plugin.websphere.StatusCodeAccessor";
    public static final String JEUS_ASYNC_OPERATION = "com.navercorp.pinpoint.plugin.jeus.async";
}
