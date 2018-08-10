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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;

import com.navercorp.pinpoint.plugin.NaverAgentPath;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Dependency({"net.sourceforge.jtds:jtds:[1.2.8]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3"})
public class JtdsIT {
    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";
    
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        
        Properties db = PropertyUtils.loadPropertyFromClassPath("database.properties");
        
        JDBC_URL = db.getProperty("mssqlserver.url");
        String[] tokens = JDBC_URL.split(":");
        
        String ip = tokens[3].substring(2);
        
        int div = tokens[4].indexOf('/');
        String port = tokens[4].substring(0, div);

        
        DB_ADDRESS = ip + ":" + port;
        DB_NAME = tokens[4].substring(div + 1);
        
        DB_ID = db.getProperty("mssqlserver.user");
        DB_PASSWORD = db.getProperty("mssqlserver.password");
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
            logger.debug("id: " + rs.getInt("id") + ", name: " + rs.getString("name") + ", age: " + rs.getInt("age"));
        } 
        
        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);
        
        conn.commit();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        
        Class<?> driverClass = Class.forName("net.sourceforge.jtds.jdbc.Driver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));
        
        Class<?> connectionClass = null;
        try {
            connectionClass = Class.forName("net.sourceforge.jtds.jdbc.ConnectionJDBC2");
        } catch (ClassNotFoundException e) {
            connectionClass = Class.forName("net.sourceforge.jtds.jdbc.JtdsConnection");
        }
        
        Method setAutoCommit = connectionClass.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(MSSQL, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(MSSQL, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Class<?> preparedStatement = Class.forName("net.sourceforge.jtds.jdbc.JtdsPreparedStatement");
        Method execute = preparedStatement.getDeclaredMethod("execute");
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));


        Class<?> statement = Class.forName("net.sourceforge.jtds.jdbc.JtdsStatement");
        
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
        Class<?> driverClass = Class.forName("net.sourceforge.jtds.jdbc.Driver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        Class<?> connectionClass = Class.forName("net.sourceforge.jtds.jdbc.ConnectionJDBC2");
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Class<?> jtdsCallableStatementClass = Class.forName("net.sourceforge.jtds.jdbc.JtdsCallableStatement");
        Method registerOutParameter = jtdsCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(3, Types.VARCHAR)));

        // JtdsPreparedStatement#execute
        Class<?> jtdsPreparedStatementClass = Class.forName("net.sourceforge.jtds.jdbc.JtdsPreparedStatement");
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
        Class<?> driverClass = Class.forName("net.sourceforge.jtds.jdbc.Driver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MSSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // ConnectionJDBC2#prepareCall(String)
        Class<?> connectionClass = Class.forName("net.sourceforge.jtds.jdbc.ConnectionJDBC2");
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(MSSQL, prepareCall, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Class<?> jtdsCallableStatementClass = Class.forName("net.sourceforge.jtds.jdbc.JtdsCallableStatement");
        Method registerOutParameter = jtdsCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        // param 1
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(MSSQL, registerOutParameter, null, DB_ADDRESS, DB_NAME, args(2, Types.INTEGER)));

        // JtdsPreparedStatement#executeQuery
        Class<?> jtdsPreparedStatementClass = Class.forName("net.sourceforge.jtds.jdbc.JtdsPreparedStatement");
        Method executeQuery = jtdsPreparedStatementClass.getDeclaredMethod("executeQuery");
        verifier.verifyTrace(event(MSSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }
}
