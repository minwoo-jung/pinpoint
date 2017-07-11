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

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion(8)
@Dependency({"mysql:mysql-connector-java:[6.min,6.max]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3"})
public class MySql_6_X_IT {

    private static MySqlItHelper HELPER;

    @BeforeClass
    public static void setup() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        Properties db = PropertyUtils.loadPropertyFromClassPath("database.properties");

        HELPER = new MySqlItHelper(db);
    }
    
    @Test
    public void testStatements() throws Exception {
        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<?> connectionClass = Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<?> preparedStatementClass = Class.forName("com.mysql.cj.jdbc.PreparedStatement");
        Class<?> statementClass = Class.forName("com.mysql.cj.jdbc.StatementImpl");

        HELPER.testStatements(driverClass, connectionClass, preparedStatementClass, statementClass);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<?> connectionClass = Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<?> callableStatementClass = Class.forName("com.mysql.cj.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_IN_OUT_parameters(driverClass, connectionClass, callableStatementClass);
    }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<?> connectionClass = Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<?> callableStatementClass = Class.forName("com.mysql.cj.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_INOUT_parameters(driverClass, connectionClass, callableStatementClass);
    }
}
