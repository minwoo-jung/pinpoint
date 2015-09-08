/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

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
import com.navercorp.pinpoint.test.plugin.Repository;

/**
 * @author Jongho Moon
 * 
 * Cannot test cubrid-jdbc 9.0.0 because it is not compatible with test db version 8.4.3.1025
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@Repository("http://maven.cubrid.org")
@Dependency({"cubrid:cubrid-jdbc:[8.2.2],[8.3.1],[8.4.4.12003],[8.5.0],[9.1.0.0212],[9.2.19.0003],[9.3.2,)"})
public class CubridIT {
    private static final String CUBRID = "CUBRID";
    private static final String CUBRID_EXECUTE_QUERY = "CUBRID_EXECUTE_QUERY";
    
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
        
        Properties db = PropertyUtils.loadPropertyFromClassPath("database.properties");
        
        JDBC_URL = db.getProperty("cubrid.url");
        String[] tokens = JDBC_URL.split(":");
        
        DB_ADDRESS = tokens[2] + ":" + tokens[3];
        DB_NAME = tokens[4];
        
        DB_ID = db.getProperty("cubrid.user");
        DB_PASSWORD = db.getProperty("cubrid.password");
    }
    
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
            System.out.println("id: " + rs.getInt("id") + ", name: " + rs.getString("name") + ", age: " + rs.getInt("age"));
        }
        
        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);
        
        conn.commit();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        
        Class<?> driverClass = Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(CUBRID, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));
        
        Class<?> connectionClass = Class.forName("cubrid.jdbc.driver.CUBRIDConnection");
        Method setAutoCommit = connectionClass.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(CUBRID, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(CUBRID, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Class<?> preparedStatement = Class.forName("cubrid.jdbc.driver.CUBRIDPreparedStatement");
        Method execute = preparedStatement.getDeclaredMethod("execute");
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));
        
        Class<?> statement = Class.forName("cubrid.jdbc.driver.CUBRIDStatement");
        Method executeQuery = statement.getDeclaredMethod("executeQuery", String.class);
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = statement.getDeclaredMethod("executeUpdate", String.class);
        verifier.verifyTrace(event(CUBRID_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getDeclaredMethod("commit");
        verifier.verifyTrace(event(CUBRID, commit, null, DB_ADDRESS, DB_NAME));
    }
}
