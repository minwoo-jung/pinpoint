/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.collector.namespace;

import org.springframework.core.NamedThreadLocal;

/**
 * @author minwoo.jung
 */
public class RequestContextHolder {

    private static final ThreadLocal<RequestAttributes> requestAttributesHolder = new NamedThreadLocal<>("Request attributes");

    public static void resetAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();

        if (attributes != null) {
            attributes.clear();
        }

        requestAttributesHolder.remove();
    }

    public static RequestAttributes getAttributes() {
        return requestAttributesHolder.get();
    }

    public static void setAttributes(RequestAttributes attributes) {
        if (attributes == null) {
            resetAttributes();
        } else {
            requestAttributesHolder.set(attributes);
        }
    }

    public static RequestAttributes currentAttributes() throws IllegalStateException {
        RequestAttributes attributes = requestAttributesHolder.get();

        if (attributes == null) {
            throw new IllegalStateException("No thread-bound RequestAttributes found ");
        }

        return attributes;
    }
}
