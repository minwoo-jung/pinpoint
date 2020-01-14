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

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.plugin.jdbc.DataBaseTestCase;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.test.plugin.jdbc.DriverProperties;
import com.navercorp.pinpoint.test.plugin.jdbc.JDBCApi;
import com.navercorp.pinpoint.test.plugin.jdbc.JDBCDriverClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Jongho Moon
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"net.sourceforge.jtds:jtds:[1.2.8]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class JtdsIT extends DataBaseTestCase {
    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";

    private static DriverProperties driverProperties;
    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    private static JdbcUrlParserV2 jdbcUrlParser;

    @BeforeClass
    public static void beforeClass() {
        driverProperties = new DriverProperties("database/jtds.properties", "mssqlserver-jtds");
        driverClass = new JtdsJDBCDriverClass(JtdsIT.class.getClassLoader());
        jdbcApi = new DefaultJDBCApi(driverClass);
        // load jdbc driver
        driverClass.getDriver();

        jdbcUrlParser = new JtdsJdbcUrlParser();
    }

    @Override
    protected JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }

    @Before
    public void before() {
        setup(MSSQL, MSSQL_EXECUTE_QUERY, driverProperties, jdbcUrlParser, jdbcApi);
    }


}
