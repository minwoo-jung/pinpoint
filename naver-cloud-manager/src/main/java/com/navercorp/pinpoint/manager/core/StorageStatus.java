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

import java.util.Objects;

/**
 * Enumeration of the current status of a storage, for example the database or hbase namespace.
 *
 * @author HyunGil Jeong
 */
public enum StorageStatus {
    NONE(0, "none"),
    READY(1, "ready"),
    CREATING(2, "creating"),
    UPDATING(3, "updating"),
    DELETING(4, "deleting"),
    ERROR(-1, "error");

    private final int code;
    private final String value;

    StorageStatus(int code, String value) {
        this.code = code;
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static StorageStatus getByCode(int code) {
        for (StorageStatus storageStatus : StorageStatus.values()) {
            if (storageStatus.code == code) {
                return storageStatus;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static StorageStatus getByValue(String value) {
        for (StorageStatus storageStatus : StorageStatus.values()) {
            if (storageStatus.value.equalsIgnoreCase(value)) {
                return storageStatus;
            }
        }
        throw new IllegalArgumentException("Unknown value : " + value);
    }
}
