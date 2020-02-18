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
package com.navercorp.pinpoint.manager.dao.mybatis;

/**
 * @author minwoo.jung
 */
public class MetadataProvider {

    private static final String SELECT_DATABASE_NAME_STATEMENT = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '%s'";
    private static final String CREATE_DATABASE_STATEMENT = "CREATE DATABASE `%s`";
    private static final String DROP_DATABASE_STATEMENT = "DROP DATABASE `%s`";

    public static String selectDatabaseName(final String name) {
        return String.format(SELECT_DATABASE_NAME_STATEMENT, name);
    }

    public static String createDatabase(final String name) {
        return String.format(CREATE_DATABASE_STATEMENT, name);
    }

    public static String dropDatabase(final String name) {
        return String.format(DROP_DATABASE_STATEMENT, name);
    }
}
