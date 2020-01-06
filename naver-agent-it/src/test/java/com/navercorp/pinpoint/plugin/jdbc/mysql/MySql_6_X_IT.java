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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.Statement;

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

    @Test
    public void testStatements() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = (Class<Connection>) Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<PreparedStatement> preparedStatementClass = (Class<PreparedStatement>) Class.forName("com.mysql.cj.jdbc.PreparedStatement");
        Class<Statement> statementClass = (Class<Statement>) Class.forName("com.mysql.cj.jdbc.StatementImpl");

        HELPER.testStatements(driverClass, connectionClass, preparedStatementClass, statementClass);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = (Class<Connection>) Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<CallableStatement> callableStatementClass = (Class<CallableStatement>) Class.forName("com.mysql.cj.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_IN_OUT_parameters(driverClass, connectionClass, callableStatementClass);
    }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.cj.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = (Class<Connection>) Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
        Class<CallableStatement> callableStatementClass = (Class<CallableStatement>) Class.forName("com.mysql.cj.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_INOUT_parameters(driverClass, connectionClass, callableStatementClass);
    }
}
