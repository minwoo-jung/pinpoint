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
package com.navercorp.pinpoint.plugin.bloc.v4;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;
import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.BlockType;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;

/**
 * @author Jongho Moon
 *
 */
@RunWith(Bloc4PluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion({7, 8})
@TraceObjectManagable
public class BlocIT {
    private static final int HTTP_PORT = 5098;
    private static final String BLOC = "BLOC";
    
    @Test
    public void testServerType() {
        PluginTestVerifier agent = PluginTestVerifierHolder.getInstance();
        agent.verifyServerType(BLOC);
    }
    
    @Test
    public void testInvocation() throws Exception {
        String path = "/test/hello/sayHello";
        String queryString = "name=pinpoint";
        String pathWithQueryString = path + "?" + queryString;
        URL url = new URL("http://localhost:" + HTTP_PORT + pathWithQueryString);
        
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCachedApis(System.out);
        verifier.printBlocks(System.out);
        
        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR");
        
        verifier.verifyTraceBlock(BlockType.EVENT, ServiceType.INTERNAL_METHOD.getName(), annotation("CALL_URL", path), annotation("PROTOCOL", "http"));
        verifier.verifyTraceBlock(BlockType.ROOT, BLOC, annotation("http.url", pathWithQueryString), annotation("http.param", queryString));
        verifier.verifyTraceBlockCount(0);
    }
}
