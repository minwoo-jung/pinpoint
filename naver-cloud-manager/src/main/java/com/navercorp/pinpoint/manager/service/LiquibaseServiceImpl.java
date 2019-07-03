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
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Service
public class LiquibaseServiceImpl implements LiquibaseService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataSource dataSource;

    public LiquibaseServiceImpl(@Qualifier("originalDataSource") DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @Override
    public SchemaStatus getDatabaseSchemaStatus(String databaseName) throws SQLException, LiquibaseException {
        Connection connection = dataSource.getConnection();
        connection.setCatalog(databaseName);
        Database database = null;
        try {
            final Contexts contexts = new Contexts();
            final LabelExpression labelExpression = new LabelExpression();

            try {
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            } catch (DatabaseException e) {
                logger.error("Error getting database : {}", databaseName, e);
                return SchemaStatus.NONE;
            }

            Liquibase liquibase = new Liquibase("liquibase/pinpoint-table-schema.xml", new ClassLoaderResourceAccessor(), database);
            DatabaseChangeLog databaseChangeLog = liquibase.getDatabaseChangeLog();
            try {
                liquibase.checkLiquibaseTables(true, databaseChangeLog, contexts, labelExpression);
            } catch (LiquibaseException e) {
                logger.error("Error checking liquibase tables for database : {}", databaseName, e);
                return SchemaStatus.UNKNOWN;
            }

            try {
                databaseChangeLog.validate(database, contexts, labelExpression);
            } catch (LiquibaseException e) {
                return SchemaStatus.INVALID;
            }

            List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(contexts, labelExpression);
            if (CollectionUtils.isEmpty(unrunChangeSets)) {
                return SchemaStatus.VALID;
            }
            return SchemaStatus.VALID_OUT_OF_DATE;
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    connection.close();
                }
            } else {
                connection.close();
            }
        }
    }

    @Override
    public void createRepository(String databaseName) throws SQLException, LiquibaseException {
        Connection connection = dataSource.getConnection();
        connection.setCatalog(databaseName);
        Database database = null;
        try {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("liquibase/pinpoint-table-schema.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts());
        } catch (LiquibaseException e) {
            logger.error("fail to create Repository by liquibase. databaseName :" + databaseName, e);
            throw e;
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    connection.close();
                }
            } else {
                connection.close();
            }
        }
    }

    @Override
    public void updateSchema(String databaseName) throws SQLException, LiquibaseException {
        Connection connection = dataSource.getConnection();
        connection.setCatalog(databaseName);
        Database database = null;
        try {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("liquibase/pinpoint-table-schema.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts());
        } catch (LiquibaseException e) {
            logger.error("fail to create Repository by liquibase. databaseName :" + databaseName, e);
            throw e;
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    connection.close();
                }
            } else {
                connection.close();
            }
        }
    }
}
