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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

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
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion(8)
@Dependency({"org.postgresql:postgresql:[9.min,9.4.1207)", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5", "com.nhncorp.nelo2:nelo2-java-sdk-log4j:1.3.3"})
public class PostgreSql_9_x_to_9_4_1207_IT {

    private static PostgreSqlItHelper HELPER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Properties databaseProperties = PropertyUtils.loadPropertyFromClassPath("database.properties");
        HELPER = new PostgreSqlItHelper(databaseProperties);
    }

    @Test
    public void testStatements() throws Exception {
        final Class<?> driverClazz = Class.forName("org.postgresql.Driver");
        final Class<?> connectionClazz = Class.forName("org.postgresql.jdbc2.AbstractJdbc2Connection");
        final Class<?> abstractJdbc2StatementClazz = Class.forName("org.postgresql.jdbc2.AbstractJdbc2Statement");
        final Class<?> abstractJdbc3StatementClazz = Class.forName("org.postgresql.jdbc3.AbstractJdbc3Statement");;

        HELPER.testStatements(driverClazz, connectionClazz, abstractJdbc2StatementClazz, abstractJdbc2StatementClazz, abstractJdbc3StatementClazz);
    }
}
