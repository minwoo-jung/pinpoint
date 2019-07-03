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
package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.core.SchemaStatus;
import liquibase.exception.LiquibaseException;

import java.sql.SQLException;

/**
 * @author minwoo.jung
 */
public interface LiquibaseService {

    SchemaStatus getDatabaseSchemaStatus(String databaseName) throws SQLException, LiquibaseException;

    void createRepository(String databaseName) throws SQLException, LiquibaseException;

    void updateSchema(String databaseName) throws SQLException, LiquibaseException;
}
