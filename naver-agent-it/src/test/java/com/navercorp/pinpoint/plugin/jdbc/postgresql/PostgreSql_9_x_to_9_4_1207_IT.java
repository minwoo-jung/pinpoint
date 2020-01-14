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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@JvmVersion(8)
@Dependency({"org.postgresql:postgresql:[9.min,9.4.1207)", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class PostgreSql_9_x_to_9_4_1207_IT {

    private static PostgreSqlItHelper HELPER;
    
    private static final PostgreSqlJDBCDriverClass jdbcDriverClass = new PostgreSql_9_x_to_9_4_1207_JDBCDriverClass();

    private final PostgreSqlJDBCApi jdbcMethod = new PostgreSqlJDBCApi(jdbcDriverClass);

    @BeforeClass
    public static void beforeClass() throws Exception {
        jdbcDriverClass.getDriver();

        DriverProperties driverProperties = new DriverProperties("database/postgre.properties", "postgresql");
        HELPER = new PostgreSqlItHelper(driverProperties);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

    @Test
    public void testStatements() throws Exception {
        HELPER.testStatements(jdbcMethod);
    }
}
