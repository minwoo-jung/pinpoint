/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.plugin.jdbc.cubrid.CubridJdbcUrlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author HyunGil Jeong
 */
public class PostgreSqlItHelper {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String POSTGRESQL = "POSTGRESQL";
    private static final String POSTGRESQL_EXECUTE_QUERY = "POSTGRESQL_EXECUTE_QUERY";

    private final String jdbcUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final String databaseAddress;
    private final String databaseName;

    PostgreSqlItHelper(DriverProperties driverProperties) {
        if (driverProperties == null) {
            throw new NullPointerException("driverProperties");
        }
        jdbcUrl = driverProperties.getUrl();
        JdbcUrlParserV2 jdbcUrlParser = new PostgreSqlJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(jdbcUrl);

        databaseAddress = databaseInfo.getHost().get(0);
        databaseName = databaseInfo.getDatabaseId();

        databaseUser = driverProperties.getUser();
        databasePassword = driverProperties.getPassword();
    }

    void testStatements(
            Class<?> driverClazz,
            Class<?> connectionClazz,
            Class<?> insertStatementClazz,
            Class<?> selectStatementClazz,
            Class<?> deleteStatementClazz) throws Exception {
        final String name = "testUser";
        final int age = 5;

        Connection conn = DriverManager.getConnection(jdbcUrl, databaseUser, databasePassword);

        conn.setAutoCommit(false);

        String insertQuery = "INSERT INTO pinpoint.test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM pinpoint.test";
        String deleteQuery = "DELETE FROM pinpoint.test";

        PreparedStatement insertPreparedStatement = conn.prepareStatement(insertQuery);
        insertPreparedStatement.setString(1, name);
        insertPreparedStatement.setInt(2, age);
        insertPreparedStatement.execute();

        Statement selectStatement = conn.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectQuery);

        while (rs.next()) {
            logger.debug("name : " + rs.getString("name") + ", age : " + rs.getInt("age"));
        }

        Statement deleteStatement = conn.createStatement();
        deleteStatement.executeUpdate(deleteQuery, Statement.NO_GENERATED_KEYS);

        conn.commit();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        // Driver#connect(String, Properties)
        Method connect = driverClazz.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(POSTGRESQL, connect, null, databaseAddress, databaseName, cachedArgs(jdbcUrl)));

        // Connection#setAutoCommit(boolean)
        Method setAutoCommit = connectionClazz.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(POSTGRESQL, setAutoCommit, null, databaseAddress, databaseName, args(false)));

        // Connection#prepareStatement(String) - prepare insert
        Method prepareStatement = connectionClazz.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(POSTGRESQL, prepareStatement, null, databaseAddress, databaseName, sql(insertQuery, null)));

        // PreparedStatement#execute() - execute insert
        Method execute = insertStatementClazz.getDeclaredMethod("execute");
        verifier.verifyTrace(event(POSTGRESQL_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(insertQuery, null, name + ", " + age)));

        // Statement#executeQuery(String) - execute select
        Method executeQuery = selectStatementClazz.getDeclaredMethod("executeQuery", String.class);
        verifier.verifyTrace(event(POSTGRESQL_EXECUTE_QUERY, executeQuery, null, databaseAddress, databaseName, Expectations.sql(selectQuery, null)));

        // Statement#executeUpdate(String, int) - execute delete
        Method executeUpdate = deleteStatementClazz.getDeclaredMethod("executeUpdate", String.class, int.class);
        verifier.verifyTrace(event(POSTGRESQL_EXECUTE_QUERY, executeUpdate, null, databaseAddress, databaseName, Expectations.sql(deleteQuery, null)));

        // Connection#commit()
        Method commit = connectionClazz.getDeclaredMethod("commit");
        verifier.verifyTrace(event(POSTGRESQL, commit, null, databaseAddress, databaseName));
    }
}
