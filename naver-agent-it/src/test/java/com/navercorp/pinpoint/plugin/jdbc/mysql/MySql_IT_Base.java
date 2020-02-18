/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.test.plugin.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.test.plugin.jdbc.JDBCDriverClass;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public abstract class MySql_IT_Base {

    
    protected abstract JDBCDriverClass getJDBCDriverClass();

    protected abstract DriverProperties getDriverProperties();

    public Connection getConnection(String url) throws SQLException {
        DriverProperties driverProperties = getDriverProperties();
        final String user = driverProperties.getUser();
        final String password = driverProperties.getPassword();

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        return DriverManager.getConnection(url, properties);
    }

    @Before
    public void before() throws Exception {
        JDBCDriverClass driverClass = getJDBCDriverClass();
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @After
    public void after() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

}