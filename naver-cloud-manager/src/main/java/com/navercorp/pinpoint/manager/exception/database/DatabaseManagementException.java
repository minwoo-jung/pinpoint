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

package com.navercorp.pinpoint.manager.exception.database;

/**
 * @author HyunGil Jeong
 */
public class DatabaseManagementException extends RuntimeException {

    private final String databaseName;

    public DatabaseManagementException(String databaseName, String message) {
        super(message);
        this.databaseName = databaseName;
    }

    public DatabaseManagementException(String databaseName, String message, Throwable cause) {
        super(message, cause);
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
