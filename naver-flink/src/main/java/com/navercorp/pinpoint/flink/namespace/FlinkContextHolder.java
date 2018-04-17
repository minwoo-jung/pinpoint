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
package com.navercorp.pinpoint.flink.namespace;

import org.springframework.core.NamedThreadLocal;

/**
 * @author minwoo.jung
 */
public class FlinkContextHolder {

    private static final ThreadLocal<FlinkAttributes> flinkAttributesHolder = new NamedThreadLocal<>("Flink attributes");

    public static void resetAttributes() {
        FlinkAttributes attributes = flinkAttributesHolder.get();

        if (attributes != null) {
            attributes.clear();
        }

        flinkAttributesHolder.remove();
    }

    public static FlinkAttributes getAttributes() {
        return flinkAttributesHolder.get();
    }

    public static void setAttributes(FlinkAttributes attributes) {
        if (attributes == null) {
            resetAttributes();
        } else {
            flinkAttributesHolder.set(attributes);
        }
    }

    public static FlinkAttributes currentAttributes() throws IllegalStateException {
        FlinkAttributes attributes = flinkAttributesHolder.get();

        if (attributes == null) {
            throw new IllegalStateException("No thread-bound FlinkAttributes found ");
        }

        return attributes;
    }
}
