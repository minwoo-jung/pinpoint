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
package com.navercorp.pinpoint.plugin.nelo.naver;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.plugin.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.*;
import com.navercorp.test.Echo;
import com.navercorp.test.Empty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author minwoo.jung
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({ "com.naver.nelo2:nelo2-java-sdk-logback:[1.6.0,]", "ch.qos.logback:logback-classic:[1.2.3]", "org.slf4j:slf4j-api:[1.7.2]"})
@JvmVersion(7)
@JvmArgument("-Dlogback.configurationFile=com/navercorp/pinpoint/plugin/nelo/naver/logback.xml")
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
