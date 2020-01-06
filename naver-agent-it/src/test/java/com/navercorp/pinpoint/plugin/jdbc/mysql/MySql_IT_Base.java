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

import com.navercorp.pinpoint.plugin.DriverManagerUtils;
import com.navercorp.pinpoint.plugin.jdbc.DriverProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.DriverManager;

/**
 * @author Taejin Koo
 */
public class MySql_IT_Base {

    
    protected static DriverProperties driverProperties;
    protected static MySqlItHelper HELPER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());

        driverProperties = new DriverProperties("database/mysql.properties", "mysql");
        HELPER = new MySqlItHelper(driverProperties);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

}
