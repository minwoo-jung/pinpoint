/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.common.util.DelegateEnumeration;
import com.navercorp.pinpoint.common.util.EmptyEnumeration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public enum LucyNetHeader {

    PINPOINT_TRACE_ID(Header.HTTP_TRACE_ID.toString()),
    PINPOINT_SPAN_ID(Header.HTTP_SPAN_ID.toString()),
    PINPOINT_PARENT_SPAN_ID(Header.HTTP_PARENT_SPAN_ID.toString()),
    PINPOINT_SAMPLED(Header.HTTP_SAMPLED.toString()),
    PINPOINT_FLAGS(Header.HTTP_FLAGS.toString()),
    PINPOINT_PARENT_APPLICATION_NAME(Header.HTTP_PARENT_APPLICATION_NAME.toString()),
    PINPOINT_PARENT_APPLICATION_TYPE(Header.HTTP_PARENT_APPLICATION_TYPE.toString()),
    PINPOINT_HOST(Header.HTTP_HOST.toString());

    private String name;

    LucyNetHeader(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    private static final Map<String, LucyNetHeader> NAME_SET = createMap();

    private static Map<String, LucyNetHeader> createMap() {
        LucyNetHeader[] headerList = values();
        Map<String, LucyNetHeader> map = new HashMap<String, LucyNetHeader>(headerList.length);
        for (LucyNetHeader header : headerList) {
            map.put(header.name, header);
        }
        return map;
    }

    public static LucyNetHeader getHeader(String name) {
        if (name == null) {
            return null;
        }
        if (!startWithPinpointHeader(name)) {
            return null;
        }
        return NAME_SET.get(name);
    }



    public static boolean hasHeader(String name) {
        return getHeader(name) != null;
    }

    public static Enumeration getHeaders(String name) {
        if (name == null) {
            return null;
        }
        final LucyNetHeader header = getHeader(name);
        if (header == null) {
            return null;
        }
        // if pinpoint header
        return new EmptyEnumeration();
    }

    public static Enumeration filteredHeaderNames(final Enumeration enumeration) {
        return new DelegateEnumeration(enumeration, FILTER);
    }

    private static DelegateEnumeration.Filter FILTER = new DelegateEnumeration.Filter() {
        @Override
        public boolean filter(Object o) {
            if (o instanceof String) {
                return hasHeader((String )o);
            }
            return false;
        }
    };

    private static boolean startWithPinpointHeader(String name) {
        return name.startsWith("Pinpoint-");
    }

}
