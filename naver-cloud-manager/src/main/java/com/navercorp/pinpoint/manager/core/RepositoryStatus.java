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
 * @author HyunGil Jeong
 */
public enum RepositoryStatus {
    NOT_READY,
    CREATING,
    READY,
    ERROR;

    public static RepositoryStatus fromStorageStatuses(StorageStatus databaseStatus, StorageStatus hbaseStatus) {
        if (databaseStatus == null || hbaseStatus == null) {
            return ERROR;
        }
        if (databaseStatus == StorageStatus.ERROR || hbaseStatus == StorageStatus.ERROR) {
            return ERROR;
        }
        if (databaseStatus == StorageStatus.DELETING || hbaseStatus == StorageStatus.DELETING) {
            return NOT_READY;
        }
        if (databaseStatus == StorageStatus.NONE || hbaseStatus == StorageStatus.NONE) {
            return NOT_READY;
        }
        if (databaseStatus == StorageStatus.CREATING || hbaseStatus == StorageStatus.CREATING) {
            return CREATING;
        }
        return READY;
    }
}
