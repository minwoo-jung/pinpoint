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
package com.navercorp.pinpoint.plugin.jdbc.mysql;

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

/**
 * @author Jongho Moon
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@Dependency({"mysql:mysql-connector-java:[5.0.8],[5.1.36,)"})
public class MySqlIT {
    private static final String MYSQL = "MYSQL";
    private static final String MYSQL_EXECUTE_QUERY = "MYSQL_EXECUTE_QUERY";
    
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        
        Properties db = PropertyUtils.loadPropertyFromClassPath("database.properties");
        
        JDBC_URL = db.getProperty("mysql.url");
        String[] tokens = JDBC_URL.split(":");
        
        String ip = tokens[2].substring(2);
        
        int div = tokens[3].indexOf('/');
        int question = tokens[3].indexOf('?');

        String port = tokens[3].substring(0, div);

        
        DB_ADDRESS = ip + ":" + port;
        DB_NAME = question == -1 ? tokens[3].substring(div + 1) : tokens[3].substring(div + 1, question);
        
        DB_ID = db.getProperty("mysql.user");
        DB_PASSWORD = db.getProperty("mysql.password");
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
        
        Class<?> driverClass = Class.forName("com.mysql.jdbc.NonRegisteringDriver");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(MYSQL, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));
        
        Class<?> connectionClass = null;
        try {
            connectionClass = Class.forName("com.mysql.jdbc.ConnectionImpl");
        } catch (ClassNotFoundException e) {
            connectionClass = Class.forName("com.mysql.jdbc.Connection");
        }
        
        Method setAutoCommit = connectionClass.getDeclaredMethod("setAutoCommit", boolean.class);
        verifier.verifyTrace(event(MYSQL, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));
        

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class);
        verifier.verifyTrace(event(MYSQL, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));
        
        Class<?> preparedStatement = Class.forName("com.mysql.jdbc.PreparedStatement");
        Method execute = preparedStatement.getDeclaredMethod("execute");
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));
        
        Class<?> statement = null;
        try {
            statement = Class.forName("com.mysql.jdbc.StatementImpl");
        } catch (ClassNotFoundException e) {
            statement = Class.forName("com.mysql.jdbc.Statement");
        }
        
        Method executeQuery = statement.getDeclaredMethod("executeQuery", String.class);
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));
        
        Method executeUpdate = statement.getDeclaredMethod("executeUpdate", String.class);
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));
        
        Method commit = connectionClass.getDeclaredMethod("commit");
        verifier.verifyTrace(event(MYSQL, commit, null, DB_ADDRESS, DB_NAME));
    }
}
