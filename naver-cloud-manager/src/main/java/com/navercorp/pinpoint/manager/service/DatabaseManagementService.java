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

package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.core.SchemaStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.exception.database.DatabaseManagementException;
import com.navercorp.pinpoint.manager.exception.database.DuplicateDatabaseException;
import com.navercorp.pinpoint.manager.exception.database.UnknownDatabaseException;
import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import com.navercorp.pinpoint.manager.vo.user.UserInfo;

import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public interface DatabaseManagementService {

    /**
     * Returns {@code true} if a database with the given {@code databaseName} exists.
     *
     * @param databaseName name of the database to check for existence
     * @return {@code true} if the database exists
     */
    boolean databaseExists(String databaseName);

    /**
     * Returns the current status of the database schema.
     *
     * @param databaseName name of the database to check the schema status
     * @return current status of the database schema
     */
    SchemaStatus getSchemaStatus(String databaseName);

    /**
     * Returns the current database information.
     *
     * @param databaseName name of the database to retrieve the information from
     * @return current database information
     * @throws UnknownDatabaseException if no database named {@code databaseName} exists
     */
    DatabaseInfo getDatabaseInfo(String databaseName);

    /**
     * Updates the current database status to {@code newStatus} if the current status is one of those specified in
     * {@code validStatuses}, and returns {@code true}. Returns {@code false} if the current database status is not
     * one of those specified in {@code validStatuses}, and no change is made to the current database status.
     *
     * @param databaseName name of the database to update the status
     * @param newStatus new status to update to
     * @param validStatuses valid current statuses to carry out the update
     * @return {@code true} if database status has been successfully updated, {@code false} if otherwise
     * @throws DatabaseManagementException if there was a problem updating database status
     */
    boolean checkAndUpdateDatabaseStatus(String databaseName, StorageStatus newStatus, Set<StorageStatus> validStatuses);

    /**
     * Starts creating a new database with the specified {@code databaseName}, initializing the schema to serve as a
     * Pinpoint database.
     * Returns {@code true} if database creation is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful creation of the database.
     *
     * @param databaseName name of the database to create
     * @return {@code true} if database creation is successfully started
     * @throws DuplicateDatabaseException if a database with the same name already exists
     * @throws DatabaseManagementException if there was a problem updating database status
     */
    boolean createDatabase(String databaseName);

    /**
     * Starts creating a new database with the specified {@code databaseName}, initializing the schema to serve as a
     * Pinpoint database.
     * Additionally, this method inserts an admin user with {@code userInfo} and {@code password}.
     * Returns {@code true} if database creation is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful creation of the database.
     *
     * @param databaseName name of the database to create
     * @param userInfo admin user info to insert
     * @param password password of the admin user to insert
     * @return {@code true} if database creation is successfully started
     * @throws DuplicateDatabaseException if a database with the same name already exists
     * @throws DatabaseManagementException if there was a problem updating database status
     */
    boolean createDatabaseWithUser(String databaseName, UserInfo userInfo, String password);

    /**
     * Starts updating the schema of the database specified by {@code databaseName} to the latest version.
     * Returns {@code true} if the schema update is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful completion of schema update.
     *
     * @param databaseName name of the database to update the schema
     * @return {@code true} if schema update is successully started
     * @throws UnknownDatabaseException if no database named {@code databaseName} exists
     * @throws DatabaseManagementException if there was a problem updating database status
     */
    boolean updateDatabase(String databaseName);

    /**
     * Starts deleting (drop) the database specified by {@code databaseName}.
     * Returns {@code true} if database deletion is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successul deletion of the database.
     *
     * @param databaseName name of the database to delete
     * @return {@code true} if database deletion is successully started
     * @throws UnknownDatabaseException if no database named {@code databaseName} exists
     * @throws DatabaseManagementException if there was a problem updating database status
     */
    boolean dropDatabase(String databaseName);
}
