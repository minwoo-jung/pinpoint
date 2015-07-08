package com.navercorp.pinpoint.plugin.nelo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.test.Echo;
import com.navercorp.test.Empty;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@Repository("http://repo.nhncorp.com/maven2")
@Dependency({ "com.nhncorp.nelo2:nelo2-java-sdk-logback:[1.3.3,)", "ch.qos.logback:logback-classic:[1.0.13]", "org.slf4j:slf4j-api:[1.7.2]", "org.slf4j:slf4j-log4j12:[1.7.5]","org.apache.thrift:libthrift:[0.9.0]", "commons-lang:commons-lang:[2.6]"})
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
        verifier.verifyIsLoggingTransactionInfo(true);
        verifier.verifyTraceCount(0);

    }
    
    @Test
    public void test2() throws Exception {
        Empty empty = new Empty();
        empty.empty();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyIsLoggingTransactionInfo(false);
        verifier.verifyTraceCount(0);
    }
}
