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
package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.DriverManagerUtils;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.plugin.jdbc.mysql.MySqlJdbcUrlParser;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
* @author Woonduk Kang(emeroad)
*/
@Ignore
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@PinpointConfig("pinpoint-mssql.config")
@Dependency({"com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8", NaverAgentPath.TEST_IT,
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class MSSqlIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";

    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    @BeforeClass
    public static void setup() throws Exception {

        getDriverClass();

        DriverProperties driverProperties = new DriverProperties("database/mssql.properties", "mssqlserver");
        
        JDBC_URL = driverProperties.getUrl();
        JdbcUrlParserV2 jdbcUrlParser = new MySqlJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(JDBC_URL);
        DB_ADDRESS = databaseInfo.getHost().get(0);
        DB_NAME = databaseInfo.getDatabaseId();

//        jdbc:sqlserver://10.106.149.189:1433;databaseName=pinpoint_test
        DB_ID = driverProperties.getUser();
        DB_PASSWORD = driverProperties.getPassword();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
    }

    private static Class<?> getDriverClass() throws ClassNotFoundException {
        return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    private Class<?> getConnectionClass() throws ClassNotFoundException {
        return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerConnection");
    }

    private Class<?> getStatement() throws ClassNotFoundException {
        return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerStatement");
    }

    private Class<?> getPreparedStatementClass() throws ClassNotFoundException {
        return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement");
    }

    private Class<?> getCallableStatementClass() throws ClassNotFoundException {
        return Class.forName("com.microsoft.sqlserver.jdbc.SQLServerCallableStatement");
    }

    @Test
    public void test() throws Exception {
        Connection conn = getConnection();
        
        conn.setAutoCommit(false);
        
        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";
        
        PreparedStatement insert = conn.prepareStatement(insertQuery);
        insert.setString(1, "maru");
        insert.setInt(2, 5);
        insert.execute();
        
        Statement select = conn.createStatement();
        ResultSet rs = select.executeQuery(selectQuery);
        
        while (rs.next()) {
            logger.debug("id: " + rs.getInt("id") + ", name: " + rs.getString("name") + ", age: " + rs.getInt("age"));
        } 
        
        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);
        
        conn.commit();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        
        Class<?> driverClass = getDriverClass();
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        Class<?> connectionClass = getConnectionClass();

        Method setAutoCommit = connectionClass.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(MSSQL, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(MSSQL, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));

        Class<?> preparedStatement = getPreparedStatementClass();
        Method execute = preparedStatement.getDeclaredMethod("execute");
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));


        Class<?> statement = getStatement();

        Method executeQuery = statement.getDeclaredMethod("executeQuery", String.class);
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = statement.getDeclaredMethod("executeUpdate", String.class);
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getDeclaredMethod("commit");
        verifier.verifyTrace(event(MSSQL, commit, null, DB_ADDRESS, DB_NAME));
    }



    /*
        CREATE PROCEDURE concatCharacters
            @a CHAR(1),
            @b CHAR(1),
            @c CHAR(2) OUTPUT
        AS
            SET @c = @a + @b;
     */
    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        Connection conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.VARCHAR);
        cs.execute();

        Assert.assertEquals(param1.concat(param2), cs.getString(3));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(4);

        // Driver#connect(String, Properties)
        Class<?> driverClass = getDriverClass();
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        Class<?> connectionClass = getConnectionClass();
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Class<?> jtdsCallableStatementClass = getCallableStatementClass();
        Method registerOutParameter = jtdsCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(3, Types.VARCHAR)));

        // JtdsPreparedStatement#execute
        Class<?> jtdsPreparedStatementClass = getPreparedStatementClass();
        Method execute = jtdsPreparedStatementClass.getDeclaredMethod("execute");
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }

    /*
        CREATE PROCEDURE swapAndGetSum
            @a INT OUTPUT,
            @b INT OUTPUT
        AS
            DECLARE @temp INT;
            SET @temp = @a;
            SET @a = @b;
            SET @b = @temp;
            SELECT @temp + @a;
     */
    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?) }";

        Connection conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        ResultSet rs = cs.executeQuery();

        Assert.assertTrue(rs.next());
        Assert.assertEquals(param1 + param2, rs.getInt(1));
        Assert.assertFalse(cs.getMoreResults());
        Assert.assertEquals(param2, cs.getInt(1));
        Assert.assertEquals(param1, cs.getInt(2));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(5);

        // Driver#connect(String, Properties)
        Class<?> driverClass = getDriverClass();
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        Class<?> connectionClass = getConnectionClass();
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Class<?> jtdsCallableStatementClass = getCallableStatementClass();
        Method registerOutParameter = jtdsCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        // param 1
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(2, Types.INTEGER)));

        // JtdsPreparedStatement#executeQuery
        Class<?> jtdsPreparedStatementClass = getPreparedStatementClass();
        Method executeQuery = jtdsPreparedStatementClass.getDeclaredMethod("executeQuery");
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }

}
