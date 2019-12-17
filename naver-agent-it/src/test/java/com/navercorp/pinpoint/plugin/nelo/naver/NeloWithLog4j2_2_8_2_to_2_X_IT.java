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
import com.navercorp.pinpoint.plugin.NaverAgentPath;
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
@Dependency({"org.apache.logging.log4j:log4j-core:[2.8.2,2.13)", "org.apache.logging.log4j:log4j-api:[2.12.1]", "org.apache.logging.log4j:log4j-slf4j-impl:[2.12.1]", "com.naver.nelo2:nelo2-java-sdk-log4j2:[2.8.2]"})
@JvmArgument("-Dlog4j.configurationFile=com/navercorp/pinpoint/plugin/nelo/naver/log4j2.xml")
@JvmVersion(7)
@PinpointConfig("pinpoint.config")
public class NeloWithLog4j2_2_8_2_to_2_X_IT {
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
