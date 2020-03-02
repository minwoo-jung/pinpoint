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
package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.jdbc.*;
import com.navercorp.pinpoint.pluginit.utils.NaverAgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author Jongho Moon
 * 
 * Cannot test cubrid-jdbc 9.0.0 because it is not compatible with test db version 8.4.3.1025
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://maven.cubrid.org")
@Dependency({"cubrid:cubrid-jdbc:[8.2.2]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
public class CubridIT {
    private static final String CUBRID = "CUBRID";
    private static final String CUBRID_EXECUTE_QUERY = "CUBRID_EXECUTE_QUERY";

    private static DriverProperties driverProperties;
    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi ;

    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void setup() throws Exception {
        // DriverManager.setLogWriter(new PrintWriter(System.out));
        driverProperties = DriverProperties.load("database/cubrid.properties", "cubrid");
        driverClass = new CubridJDBCDriverClass();
        jdbcApi = new DefaultJDBCApi(driverClass);

        driverClass.getDriver();
        
        JDBC_URL = driverProperties.getUrl();
        JdbcUrlParserV2 jdbcUrlParser = new CubridJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(JDBC_URL);

        DB_ADDRESS = databaseInfo.getHost().get(0);
        DB_NAME = databaseInfo.getDatabaseId();

        DB_ID = driverProperties.getUser();
        DB_PASSWORD = driverProperties.getPassword();
    }

    @Before
    public void registerDriver() throws Exception {
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @After
    public void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }
    
    @Test
    public void test() throws Exception {
        System.out.println("classLoader"+ this.getClass().getClassLoader());
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
        verifier.verifyTrace(event(CUBRID, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(event(CUBRID, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        
        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(event(CUBRID, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Method execute = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));

        JDBCApi.StatementClass statementClass = jdbcApi.getStatement();
        Method executeQuery = statementClass.getExecuteQuery();
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = statementClass.getExecuteUpdate();
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getCommit();
        verifier.verifyTrace(event(CUBRID, commit, null, DB_ADDRESS, DB_NAME));
    }
}
