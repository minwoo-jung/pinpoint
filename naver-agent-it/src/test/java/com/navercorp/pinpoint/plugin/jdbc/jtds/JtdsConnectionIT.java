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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;

/**
 * @author emeroad
 */
public class JtdsConnectionIT extends BasePinpointTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Properties db;

    @BeforeClass
    public static void beforeClass() throws Exception {
        db = PropertyUtils.loadPropertyFromClassPath("database.properties");
    }

    @Test
    public void executeQueryAndExecuteUpdate() throws SQLException {
        Connection connection = connectDB();

        PreparedStatement preparedStatement = connection.prepareStatement("select 1 ");
//        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            logger.debug("---{}", resultSet.getObject(1));
        }
        connection.close();
    }

    private Connection connectDB() throws SQLException {
        String url = db.getProperty("mssqlserver.url");
        String user = db.getProperty("mssqlserver.user");
        String password = db.getProperty("mssqlserver.password");

        Driver driver = new net.sourceforge.jtds.jdbc.Driver();
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        return driver.connect(url, properties);
    }


    @Test
    public void testModify() throws Exception {
        logger.debug("testModify");

        Connection connection = connectDB();

        logger.debug("Connection class name:{}", connection.getClass().getName());
        logger.debug("Connection class cl:{}", connection.getClass().getClassLoader());

        DatabaseInfo url = ((DatabaseInfoAccessor) connection)._$PINPOINT$_getDatabaseInfo();
        Assert.assertNotNull(url);
        List<SpanEventBo> currentSpanEvents = getCurrentSpanEvents();
        logger.debug("{}", currentSpanEvents);
//        Assert.assertEquals(1, currentSpanEvents.size());

        statement(connection);
        currentSpanEvents = getCurrentSpanEvents();
        logger.debug("{}", currentSpanEvents);
//        Assert.assertEquals(2, currentSpanEvents.size());

        preparedStatement(connection);

        preparedStatement2(connection);

        preparedStatement3(connection);

        preparedStatement4(connection);

        preparedStatement5(connection);

        preparedStatement6(connection);

        preparedStatement7(connection);

        preparedStatement8(connection);


        connection.close();
        DatabaseInfo clearUrl = ((DatabaseInfoAccessor) connection)._$PINPOINT$_getDatabaseInfo();
        Assert.assertNull(clearUrl);

    }


    private void statement(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        statement.close();
    }

    private void preparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select 1");
        logger.debug("PreparedStatement className:" + preparedStatement.getClass().getName());
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
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
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
        int[] columnIndex = {1};
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
