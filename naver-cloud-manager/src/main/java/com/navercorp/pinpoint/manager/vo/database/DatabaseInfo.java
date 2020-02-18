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

package com.navercorp.pinpoint.manager.vo.database;

import com.navercorp.pinpoint.manager.core.SchemaStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;

/**
 * Wrapper class containing various information about the database.
 *
 * @author HyunGil Jeong
 */
public class DatabaseInfo {

    private String databaseName;
    private StorageStatus databaseStatus;
    private SchemaStatus databaseSchemaStatus;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public StorageStatus getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(StorageStatus databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public SchemaStatus getDatabaseSchemaStatus() {
        return databaseSchemaStatus;
    }

    public void setDatabaseSchemaStatus(SchemaStatus databaseSchemaStatus) {
        this.databaseSchemaStatus = databaseSchemaStatus;
    }
}