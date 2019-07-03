/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.manager.core;

/**
 * Enumeration of current schema status, obtained by comparing the executed change logs against
 * a certain set of predefined change sets.
 *
 * @author HyunGil Jeong
 */
public enum SchemaStatus {
    ERROR("error", "Error checking schema") {
        @Override
        public boolean isValid() {
            return false;
        }
    },
    NONE("none", "Schema does not exist") {
        @Override
        public boolean isValid() {
            return false;
        }
    },
    UNKNOWN("unknown", "Schema exists but there is no change log") {
        @Override
        public boolean isValid() {
            return false;
        }
    },
    VALID("valid", "Schema valid and up to date") {
        @Override
        public boolean isValid() {
            return true;
        }
    },
    VALID_OUT_OF_DATE("valid - out of date", "Schema valid but update needed") {
        @Override
        public boolean isValid() {
            return true;
        }
    },
    INVALID("invalid", "Schema exists but does not match change sets") {
        @Override
        public boolean isValid() {
            return false;
        }
    };

    private final String value;
    private final String detailedMessage;

    SchemaStatus(String value, String detailedMessage) {
        this.value = value;
        this.detailedMessage = detailedMessage;
    }

    public abstract boolean isValid();

    public String getValue() {
        return value;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }
}
