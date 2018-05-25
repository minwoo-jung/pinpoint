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
package com.navercorp.pinpoint.manager.liquibase;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.sql.Connection;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-liquibase.xml"})
@Ignore
public class CreateSchemaTest {

    @Autowired
    DataSource dataSource;

    @Test
    public void metadataTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        try {
        Liquibase liquibase = new Liquibase("db/liquibase/metadata/metadata-table-schema.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts());
        } finally {
            if (database != null) {
                database.close();
            } else {
                connection.close();
            }
        }
    }

    @Test
    public void pinpointTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        try {
            Liquibase liquibase = new Liquibase("db/liquibase/pinpoint-table-schema.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts());
        } finally {
            if (database != null) {
                database.close();
            } else {
                connection.close();
            }
        }
    }

    /**
     * // 현재 DB를 기반으로 ChangeLog 파일 생성
     * @throws Exception
     */
    @Test
    public void generateChangeLogTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/generateChangeLog.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.generateChangeLog(new CatalogAndSchema(null, null), new DiffToChangeLog(new DiffOutputControl()), new PrintStream("F:\\text2.txt"));
    }

}
