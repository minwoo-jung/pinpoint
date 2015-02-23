/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.tomcat;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.OnClassLoader;
import com.navercorp.pinpoint.test.plugin.TestRoot;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PluginTestProperty;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@TestRoot(path="test/tomcat/6", libraryDir={"bin", "lib"})
@JvmArgument("-Dcatalina.home=${" + PluginTestProperty.PINPOINT_TEST_DIRECTORY + "}")
@OnClassLoader(child=false)
@TraceObjectManagable
public class Tomcat6IT {
    private static Tomcat6Runner runner;
    
    private static final ServiceType TOMCAT = ServiceType.valueOf("TOMCAT");
    private static final int HTTP_PORT = 8623;

    @BeforeClass
    public static void beforeClass() throws Exception {
        String testPath = System.getProperty(PluginTestProperty.PINPOINT_TEST_DIRECTORY);
        runner = new Tomcat6Runner(testPath, "../../webapps");
        runner.start("test", HTTP_PORT);
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        runner.stop();
    }
 
    @Test
    public void testServerType() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.verifyServerType(TOMCAT);
        
        // TODO 나머지 서버 정보 확인 테스트 추가
    }
    
    @Test
    public void testRequest() throws Exception {
        URL url = new URL("http://localhost:" + HTTP_PORT);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        // TODO 요청 보내고 span 쌓인 거 확인
    }
    
    // TODO RequestFacade 제대로 처리 됐는지 테스트
}
