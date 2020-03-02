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
package com.navercorp.pinpoint.plugin.commons.dbcp;

import java.sql.Connection;
import java.util.Properties;

import com.navercorp.pinpoint.pluginit.utils.NaverAgentPath;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Jongho Moon
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Dependency({"commons-dbcp:commons-dbcp:[1.2,)", "mysql:mysql-connector-java:5.1.36"})
public class CommonsDbcpIT {
    private static final String DBCP = "DBCP";
    
    @Test
    public void test() throws Exception {
        Properties db = PropertyUtils.loadPropertyFromClassPath("database/mysql.properties");
        
        String url = db.getProperty("mysql.url");
        String username = db.getProperty("mysql.user");
        String password = db.getProperty("mysql.password");

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName("com.mysql.jdbc.Driver");
        source.setUrl(url);
        source.setUsername(username);
        source.setPassword(password);
        
        Connection connection = source.getConnection();
        connection.close();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        verifier.ignoreServiceType("MYSQL", "MYSQL_EXECUTE_QUERY");
        
        verifier.verifyTrace(Expectations.event(DBCP, BasicDataSource.class.getMethod("getConnection")));
        verifier.verifyTrace(Expectations.event(DBCP, connection.getClass().getMethod("close")));
        
        verifier.verifyTraceCount(0);
    }
}
