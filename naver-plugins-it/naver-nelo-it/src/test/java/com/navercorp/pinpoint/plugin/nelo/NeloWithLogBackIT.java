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
package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.pluginit.utils.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.test.Echo;
import com.navercorp.test.Empty;

/**
 * @author minwoo.jung
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({ "com.nhncorp.nelo2:nelo2-java-sdk-logback:[1.3.3,)", "ch.qos.logback:logback-classic:[1.0.13]", "org.slf4j:slf4j-api:[1.7.2]", "org.slf4j:slf4j-log4j12:[1.7.5]","org.apache.thrift:libthrift:[0.9.0]", "commons-lang:commons-lang:[2.6]"})
@JvmArgument("-Dlogback.configurationFile=com/navercorp/pinpoint/plugin/nelo/logback.xml")
@JvmVersion(7)
@PinpointConfig("pinpoint.config")
public class NeloWithLogBackIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void test() throws Exception {
        Echo echo = new Echo();
        echo.echo("test");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyIsLoggingTransactionInfo(LoggingInfo.LOGGED);
        verifier.verifyTraceCount(0);

    }
    
    @Test
    public void test2() throws Exception {
        Empty empty = new Empty();
        empty.empty();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyIsLoggingTransactionInfo(LoggingInfo.NOT_LOGGED);
        verifier.verifyTraceCount(0);
    }
}
