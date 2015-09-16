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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

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
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@Dependency({"oracle:ojdbc6:(,)"})
@Repository("http://repo.nhncorp.com/maven2")
public class OracleIT {
    private static final String ORACLE = "ORACLE";
    private static final String ORACLE_EXECUTE_QUERY = "ORACLE_EXECUTE_QUERY";
    
    
    private static String DB_ID;
    private static String DB_PASSWORD;
    private static String DB_ADDRESS;
    private static String DB_NAME;
    private static String JDBC_URL;

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        
        Properties db = PropertyUtils.loadPropertyFromClassPath("database.properties");
        
        JDBC_URL = db.getProperty("oracle.url");
        String[] tokens = JDBC_URL.split(":");
        
        String ip = tokens[3].substring(1);
        String port = tokens[4];

        
        DB_ADDRESS = ip + ":" + port;
        DB_NAME = tokens[5];
        
        DB_ID = db.getProperty("oracle.user");
        DB_PASSWORD = db.getProperty("oracle.password");
    }
    
//  @Test
  public void create() throws Exception {
      Connection conn = DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
      
      Statement statement = conn.createStatement();
      statement.execute("CREATE TABLE test (id INTEGER NOT NULL, name VARCHAR(45) NOT NULL, age INTEGER NOT NULL, CONSTRAINT test_pk PRIMARY KEY (id))" );
      statement.execute("CREATE SEQUENCE test_seq");
      statement.execute("CREATE OR REPLACE TRIGGER test_trigger BEFORE INSERT ON test FOR EACH ROW BEGIN SELECT test_seq.nextval INTO :new.id FROM dual; END;");
      conn.commit();
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
}
