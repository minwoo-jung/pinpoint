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
package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.test.plugin.jdbc.JDBCApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@JvmVersion(8)
@Dependency({"mysql:mysql-connector-java:[6.min,6.max]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class MySql_6_X_IT extends MySql_IT_Base {

    private final JDBCApi jdbcApi = new DefaultJDBCApi(new MySql6JDBCDriverClass());

    @Before
    public void setUp() throws Exception {
        jdbcApi.getJDBCDriverClass().getDriver();
    }

    @Test
    public void testStatements() throws Exception {
        HELPER.testStatements(jdbcApi);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        HELPER.testStoredProcedure_with_IN_OUT_parameters(jdbcApi);
    }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        HELPER.testStoredProcedure_with_INOUT_parameters(jdbcApi);
    }
}
