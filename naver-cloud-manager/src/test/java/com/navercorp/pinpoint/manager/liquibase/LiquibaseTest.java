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
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;
import java.util.Date;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-liquibase.xml"})
@Ignore
public class LiquibaseTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    DataSource dataSource2;

    @Test
    public void updateTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts());
    }

    @Test
    public void update2Test() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        Writer writer = new FileWriter(new File("F:\\text.txt"));
        liquibase.update(new Contexts(), writer);
//        liquibase.update(new Contexts());
        // 실행되는 sql도 출력시킨다.
    }

    @Test
    public void update3Test() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/naver_table.xml", new ClassLoaderResourceAccessor(), database);
        Writer writer = new FileWriter(new File("F:\\text.xml"));
        liquibase.update(new Contexts());
    }

    @Test
    public void updateTest2() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts());
    }

    @Test
    public void updateTestingRollbackTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.sql", new ClassLoaderResourceAccessor(), database);
        liquibase.updateTestingRollback(new Contexts(), new LabelExpression());
        // rollback test 하고 rollback 하고 update 다시 해서 복구
    }

    @Test
    public void rollbackTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.rollback(2, new Contexts(), new LabelExpression());
        // 1은 가장 최신 , 2는 가장 최근부터 2step 거스름
    }

    @Test
    public void rollback2Test() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.rollback(new Date(1, 11, 25, 18 , 24, 22), new Contexts(), new LabelExpression());
        //특정 시간 이후의 변경사항을 롤백
    }

    @Test
    public void futureRollbackSQLTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        Writer writer = new FileWriter(new File("F:\\text.txt"));
        liquibase.futureRollbackSQL(1, new Contexts(), new LabelExpression(), writer);
        writer.close();
        //롤백시 execute 되는 sql들을 txt 파일로 출력함
    }

    @Test
    public void difftest() throws Exception {
        Connection connection = dataSource.getConnection();
        Connection connection2 = dataSource2.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Database database2 = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection2));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        DiffResult diffResult = liquibase.diff(database, database2, new CompareControl());
        new DiffToChangeLog(diffResult, new DiffOutputControl()).print(System.out);
        //Database 자체를 diff 시키고 있다.
    }

    @Test
    public void generateChangeLogTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
        liquibase.generateChangeLog(new CatalogAndSchema(null, null), new DiffToChangeLog(new DiffOutputControl()), new PrintStream("F:\\text.txt"));
        // 현재 DB를 기반으로 ChangeLog 파일 생성
    }

    @Test
    public void generateDocumentationTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
        liquibase.generateDocumentation("F:\\pinpointDoc\\");
        // 테이블의 schema를 문서형식으로 출력한다.
    }

    @Test
    public void sqloutputTest() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/liquibase/opensource_table.xml", new ClassLoaderResourceAccessor(), database);
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
        liquibase.generateDocumentation("F:\\pinpointDoc\\");
        // 테이블의 schema를 문서형식으로 출력한다.
    }

}
