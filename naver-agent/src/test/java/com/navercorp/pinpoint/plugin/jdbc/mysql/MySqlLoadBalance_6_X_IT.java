/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author emeroad
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion(8)
@Dependency({"mysql:mysql-connector-java:[6.min,6.max]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3"})
public class MySqlLoadBalance_6_X_IT {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Properties db;

    @BeforeClass
    public static void beforeClass() throws Exception {
        db = PropertyUtils.loadPropertyFromClassPath("database.properties");
    }

    private Connection connectDB(String url) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        String user = db.getProperty("mysql.user");
        String password = db.getProperty("mysql.password");

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        return DriverManager.getConnection(url, properties);
    }

    @Test
    public void loadBalancedUrlModify() throws Exception {
        // random fail
        Connection connection = connectDB(db.getProperty("mysql.url.loadbalance"));

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());
        
        // If loadbalanced, connection is instanceof LoadBalancingConnectionProxy.
        // But we cannot cast to the type because it's loaded by another classLoader.
        // So use reflection to get currentConn field.
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(connection);
        Class<? extends InvocationHandler> aClass = invocationHandler.getClass();

        Field current = null;
        
        try {
            current = aClass.getDeclaredField("currentConn");
        } catch (NoSuchFieldException e) {
            current = aClass.getSuperclass().getDeclaredField("currentConnection");
        }
        
        current.setAccessible(true);
        Object internalConnection = current.get(invocationHandler);


        DatabaseInfo url = ((DatabaseInfoAccessor) internalConnection)._$PINPOINT$_getDatabaseInfo();
        Assert.assertNotNull(url);

        statement(connection);

        preparedStatement(connection);

        preparedStatement2(connection);

        preparedStatement3(connection);

        preparedStatement4(connection);

        preparedStatement5(connection);

        preparedStatement6(connection);

        preparedStatement7(connection);

        preparedStatement8(connection);

        connection.close();
        DatabaseInfo clearUrl = ((DatabaseInfoAccessor) internalConnection)._$PINPOINT$_getDatabaseInfo();
        Assert.assertNull(clearUrl);

    }

    private void statement(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        statement.close();
    }

    private void preparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select 1");
        logger.info("PreparedStatement className:" + preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement2(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ?");
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement3(Connection connection) throws SQLException {
        connection.setAutoCommit(false);

        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ? or id = ?  or id = ?");
        preparedStatement.setInt(1, 1);
        preparedStatement.setInt(2, 2);
        preparedStatement.setString(3, "3");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();

        connection.commit();

        connection.setAutoCommit(true);
    }


    private void preparedStatement4(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", Statement.RETURN_GENERATED_KEYS);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"});
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1, 2, 3};
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }
}
