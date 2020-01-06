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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.DriverManagerUtils;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;

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
@Dependency({"oracle:ojdbc6:(,)", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
@Repository("http://repo.navercorp.com/maven2")
public class OracleIT {
    private static final String ORACLE = "ORACLE";
    private static final String ORACLE_EXECUTE_QUERY = "ORACLE_EXECUTE_QUERY";
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        
        DriverProperties driverProperties = new DriverProperties("database/oracle.properties", "oracle");
        
        JDBC_URL = driverProperties.getUrl();

        JdbcUrlParserV2 jdbcUrlParser = new OracleJdbcUrlParser();
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

    private Connection conn;
    @After
    public void shutdown() throws Exception {
        if (this.conn != null) {
            conn.close();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
    }

    //  @Test
  public void create() throws Exception {
      this.conn = getConnection();
      
      Statement statement = conn.createStatement();
      statement.execute("CREATE TABLE test (id INTEGER NOT NULL, name VARCHAR(45) NOT NULL, age INTEGER NOT NULL, CONSTRAINT test_pk PRIMARY KEY (id))" );
      statement.execute("CREATE SEQUENCE test_seq");
      statement.execute("CREATE OR REPLACE TRIGGER test_trigger BEFORE INSERT ON test FOR EACH ROW BEGIN SELECT test_seq.nextval INTO :new.id FROM dual; END;");
      conn.commit();
  }

    @Test
    public void test() throws Exception {
        this.conn = getConnection();
        
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
        
        Class<?> driverClass = Class.forName("oracle.jdbc.driver.OracleDriver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));
        
        Class<?> connectionClass = Class.forName("oracle.jdbc.driver.PhysicalConnection");
        
        Method setAutoCommit = connectionClass.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(ORACLE, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(ORACLE, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Method execute = insert.getClass().getDeclaredMethod("execute");
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));
        
        Method executeQuery = select.getClass().getDeclaredMethod("executeQuery", String.class);
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = select.getClass().getDeclaredMethod("executeUpdate", String.class);
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getDeclaredMethod("commit");
        verifier.verifyTrace(event(ORACLE, commit, null, DB_ADDRESS, DB_NAME));
    }

    /*
        CREATE OR REPLACE PROCEDURE concatCharacters(a IN VARCHAR2, b IN VARCHAR2, c OUT VARCHAR2)
        AS
        BEGIN
            c := a || b;
        END concatCharacters;
     */
    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        this.conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.NCHAR);
        cs.execute();

        Assert.assertEquals(param1.concat(param2), cs.getString(3));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(4);

        // OracleDriver#connect(String, Properties)
        Class<?> driverClass = Class.forName("oracle.jdbc.driver.OracleDriver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        Class<?> connectionClass = Class.forName("oracle.jdbc.driver.PhysicalConnection");
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(ORACLE, prepareCall, null, DB_ADDRESS, DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        Class<?> oracleCallableStatementClass = Class.forName("oracle.jdbc.driver.OracleCallableStatementWrapper");
        Method registerOutParameter = oracleCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(3, Types.NCHAR)));

        // OraclePreparedStatementWrapper#execute
        Class<?> oraclePreparedStatementWrapper = Class.forName("oracle.jdbc.driver.OraclePreparedStatementWrapper");
        Method executeQuery = oraclePreparedStatementWrapper.getDeclaredMethod("execute");
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }

    /*
        CREATE OR REPLACE PROCEDURE swapAndGetSum(a IN OUT NUMBER, b IN OUT NUMBER, c OUT NUMBER)
        AS
        BEGIN
            c := a;
            a := b;
            b := c;
            SELECT c + a INTO c FROM DUAL;
        END swapAndGetSum;
     */
    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?, ?) }";

        this.conn = getConnection();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        cs.registerOutParameter(3, Types.INTEGER);
        cs.execute();

        Assert.assertEquals(param2, cs.getInt(1));
        Assert.assertEquals(param1, cs.getInt(2));
        Assert.assertEquals(param1 + param2, cs.getInt(3));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(6);

        // OracleDriver#connect(String, Properties)
        Class<?> driverClass = Class.forName("oracle.jdbc.driver.OracleDriver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        Class<?> connectionClass = Class.forName("oracle.jdbc.driver.PhysicalConnection");
        Method prepareCall = connectionClass.getDeclaredMethod("prepareCall", String.class);
        verifier.verifyTrace(event(ORACLE, prepareCall, null, DB_ADDRESS, DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        Class<?> oracleCallableStatementClass = Class.forName("oracle.jdbc.driver.OracleCallableStatementWrapper");
        Method registerOutParameter = oracleCallableStatementClass.getDeclaredMethod("registerOutParameter", int.class, int.class);
        // param 1
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(2, Types.INTEGER)));
        // param 3
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(3, Types.INTEGER)));

        // OraclePreparedStatementWrapper#execute
        Class<?> oraclePreparedStatementWrapper = Class.forName("oracle.jdbc.driver.OraclePreparedStatementWrapper");
        Method executeQuery = oraclePreparedStatementWrapper.getDeclaredMethod("execute");
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }


}
