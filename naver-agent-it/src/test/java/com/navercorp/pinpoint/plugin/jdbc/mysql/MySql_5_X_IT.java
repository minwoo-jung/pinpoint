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
@Dependency({"mysql:mysql-connector-java:[5.0.8],[5.1.36,5.max]", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3"})
public class MySql_5_X_IT extends MySql_IT_Base {

    @Test
    public void testStatements() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = null;
        try {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.ConnectionImpl");
        } catch (ClassNotFoundException e) {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.Connection");
        }
        Class<PreparedStatement> preparedStatementClass = (Class<PreparedStatement>) Class.forName("com.mysql.jdbc.PreparedStatement");
        Class<Statement> statementClass = null;
        try {
            statementClass = (Class<Statement>) Class.forName("com.mysql.jdbc.StatementImpl");
        } catch (ClassNotFoundException e) {
            statementClass = (Class<Statement>) Class.forName("com.mysql.jdbc.Statement");
        }

        HELPER.testStatements(driverClass, connectionClass, preparedStatementClass, statementClass);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = null;
        try {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.ConnectionImpl");
        } catch (ClassNotFoundException e) {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.Connection");
        }
        Class<CallableStatement> callableStatementClass = (Class<CallableStatement>) Class.forName("com.mysql.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_IN_OUT_parameters(driverClass, connectionClass, callableStatementClass);
    }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        Class<Driver> driverClass = (Class<Driver>) Class.forName("com.mysql.jdbc.NonRegisteringDriver");
        Class<Connection> connectionClass = null;
        try {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.ConnectionImpl");
        } catch (ClassNotFoundException e) {
            connectionClass = (Class<Connection>) Class.forName("com.mysql.jdbc.Connection");
        }
        Class<CallableStatement> callableStatementClass = (Class<CallableStatement>) Class.forName("com.mysql.jdbc.CallableStatement");

        HELPER.testStoredProcedure_with_INOUT_parameters(driverClass, connectionClass, callableStatementClass);
    }
}
