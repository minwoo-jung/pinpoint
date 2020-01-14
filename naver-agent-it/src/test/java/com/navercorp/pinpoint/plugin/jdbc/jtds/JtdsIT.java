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
package com.navercorp.pinpoint.plugin.jdbc.jtds;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.test.plugin.jdbc.JDBCApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import java.sql.Statement;
import java.sql.Types;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author Jongho Moon
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"net.sourceforge.jtds:jtds:[1.2.8]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class JtdsIT {
    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";
    
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static JDBCApi jdbcApi = new DefaultJDBCApi(new JtdsJDBCDriverClass());

    @BeforeClass
    public static void setup() throws Exception {
        // load jdbc driver
        jdbcApi.getJDBCDriverClass().getDriver();

        DriverProperties driverProperties = new DriverProperties("database/jtds.properties", "mssqlserver-jtds");

        JDBC_URL = driverProperties.getUrl();

        JdbcUrlParserV2 jdbcUrlParser = new JtdsJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(JDBC_URL);

        DB_ADDRESS = databaseInfo.getHost().get(0);
        DB_NAME = databaseInfo.getDatabaseId();

        DB_ID = driverProperties.getUser();
        DB_PASSWORD = driverProperties.getPassword();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }


//    @Test
//    public void create() throws Exception {
//        Connection conn = DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
//        
//        Statement statement = conn.createStatement();
//        statement.execute("CREATE TABLE test (id int IDENTITY(1,1), name varchar(45) NOT NULL, age int NOT NULL, PRIMARY KEY (id))" );
//        conn.commit();
//    }
    
    @Test
    public void test() throws Exception {
        Connection conn = DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
        
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
            final int id = rs.getInt("id");
            final String name = rs.getString("name");
            final int age = rs.getInt("age");
            logger.debug("id: {}, name: {}, age: {}", id, name, age);
        }
        
        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);
        
        conn.commit();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));
        
        JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(event(MSSQL, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(event(MSSQL, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Method execute = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));

        JDBCApi.StatementClass statementClass = jdbcApi.getStatement();
        Method executeQuery = statementClass.getExecuteQuery();
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = statementClass.getExecuteUpdate();
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getCommit();
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

        Connection conn = DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);

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
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Method registerOutParameter = jdbcApi.getCallableStatement().getRegisterOutParameter();
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(3, Types.VARCHAR)));

        // JtdsPreparedStatement#execute
        Method execute = jdbcApi.getPreparedStatement().getExecute();
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

        Connection conn = DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);

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
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        Method prepareCall = jdbcApi.getConnection().getPrepareCall();;
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Method registerOutParameter = jdbcApi.getCallableStatement().getRegisterOutParameter();
        // param 1
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(2, Types.INTEGER)));

        // JtdsPreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }
}
