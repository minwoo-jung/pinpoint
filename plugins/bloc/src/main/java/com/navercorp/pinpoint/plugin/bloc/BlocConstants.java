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

import static com.navercorp.pinpoint.common.AnnotationKeyProperty.*;
import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author Jongho Moon
 *
 */
public interface BlocConstants {
    public static final String FIELD_URI_ENCODING = "uriEncoding";

    public static final AnnotationKey CALL_URL = new AnnotationKey(15, "CALL_URL");
    public static final AnnotationKey CALL_PARAM = new AnnotationKey(16, "CALL_PARAM", VIEW_IN_RECORD_SET);
    public static final AnnotationKey PROTOCOL = new AnnotationKey(17, "PROTOCOL", VIEW_IN_RECORD_SET);

    public static final ServiceType BLOC = ServiceType.of(1020, "BLOC", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType BLOC_INTERNAL_METHOD = ServiceType.of(1021, "BLOC_INTERNAL_METHOD", "INTERNAL_METHOD", NORMAL_SCHEMA, CALL_URL);

}
