/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nelo.naver;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.pluginit.utils.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.*;
import com.navercorp.test.Echo;
import com.navercorp.test.Echo2;
import com.navercorp.test.Empty;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author minwoo.jung
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository({"http://repo.navercorp.com/maven2", "http://repo1.maven.org/maven2"})
@Dependency({ "com.naver.nelo2:nelo2-java-sdk-log4j2:[1.6.0,]", "org.slf4j:slf4j-api:[1.7.2]", "org.slf4j:slf4j-log4j12:[1.7.5]", "com.lmax:disruptor:[3.4.2]"})
@JvmArgument({"-Dlog4j.configurationFile=com/navercorp/pinpoint/plugin/nelo/naver/log4j2.xml", "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"})
@JvmVersion(7)
@PinpointConfig("pinpoint.config")
public class NeloWithLog4j2AsyncIT {
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
        Echo2 echo = new Echo2();
        echo.echo2("test");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyIsLoggingTransactionInfo(LoggingInfo.NOT_LOGGED);
        verifier.verifyTraceCount(0);
    }

    @Test
    public void test3() throws Exception {
        Empty empty = new Empty();
        empty.empty();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyIsLoggingTransactionInfo(LoggingInfo.NOT_LOGGED);
        verifier.verifyTraceCount(0);
    }
}
