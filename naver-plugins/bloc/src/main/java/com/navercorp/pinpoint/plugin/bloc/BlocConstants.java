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
package com.navercorp.pinpoint.plugin.bloc;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.*;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import java.nio.charset.Charset;

/**
 * @author Jongho Moon
 *
 */
public final class BlocConstants {
    private BlocConstants() {
    }

    // Common
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    public static final String UNKOWN_ADDRESS = "Unknown Address";

    public static final AnnotationKey CALL_URL = AnnotationKeyFactory.of(15, "CALL_URL");
    public static final AnnotationKey CALL_PARAM = AnnotationKeyFactory.of(16, "CALL_PARAM", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PROTOCOL = AnnotationKeyFactory.of(17, "PROTOCOL", VIEW_IN_RECORD_SET);
    
    public static final ServiceType BLOC = ServiceTypeFactory.of(1020, "BLOC", RECORD_STATISTICS);
    public static final ServiceType BLOC_INTERNAL_METHOD = ServiceTypeFactory.of(1021, "BLOC_INTERNAL_METHOD", "INTERNAL_METHOD");

    public static final String NIMM_SERVER_SOCKET_ADDRESS_ACCESSOR = "com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor";

}
