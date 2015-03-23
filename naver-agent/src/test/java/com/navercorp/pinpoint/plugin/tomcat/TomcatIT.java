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

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.catalina.util.ServerInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;

/**
 * @author Jongho Moon
 *
 */
@RunWith(TomcatPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion({7, 8})
@TraceObjectManagable
public class TomcatIT {
    private static final String TOMCAT = "TOMCAT";
    private static final String HTTP_PARAM = "http.param";

    @Test
    public void testServerType() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.verifyServerType(TOMCAT);
        verifier.verifyServerInfo(ServerInfo.getServerInfo());
        verifier.verifyConnector("HTTP/1.1", 8972);
        verifier.verifyService("Catalina/localhost/test", Arrays.asList("log4j-1.2.17.jar"));
    }
    
    @Test
    public void testRequest() throws Exception {
        String params = "param0=maru";
        String endPoint = "localhost:8972";
        String rpc = "/test/index.html";
        
        URL url = new URL("http://" + endPoint + rpc + "?" + params);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        Class<?> standardHostValve = Class.forName("org.apache.catalina.core.StandardHostValve");
        Class<?> request = Class.forName("org.apache.catalina.connector.Request");
        Class<?> response = Class.forName("org.apache.catalina.connector.Response");
        Method invoke = standardHostValve.getMethod("invoke", request, response);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printApis(System.out);
        verifier.printSpans(System.out);

        boolean removedHttpURLConnectionSpan = false;
        
        try {
            verifier.verifySpan(TOMCAT, invoke, rpc, endPoint, "127.0.0.1", annotation(HTTP_PARAM, params));
        } catch (AssertionError e) {
            // SpanEvent caused by HttpURLConnection.getResponseCode() could come first.
            verifier.verifySpan(TOMCAT, invoke, rpc, endPoint, "127.0.0.1", annotation(HTTP_PARAM, params));
            removedHttpURLConnectionSpan = true;
        }
        
        if (removedHttpURLConnectionSpan) {
            verifier.verifySpanCount(0);
        } else {
            verifier.verifySpanCount(1);
        }
    }
    
    @Test
    public void testGetHeaderNames() throws Exception {
        Set<String> names = new HashSet<String>();

        URL url = new URL("http://localhost:8972/test/getHeaderNames");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                names.add(name);
            }
        } finally {        
            connection.disconnect();
        }

        for (Header h : Header.values()) {
            String headerName = h.toString();
            assertFalse(names.contains(headerName));
        }
    }
    
    @Test
    public void testGetHeader() throws Exception {
        
        for (Header h : Header.values()) {
            URL url = new URL("http://localhost:8972/test/getHeader?name=" + h.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            String value = null;
            
            try {
                Scanner scanner = new Scanner(connection.getInputStream());
                
                if (scanner.hasNextLine()) {
                    value = scanner.nextLine();
                } else {
                    fail("No response");
                }
            } finally {
                connection.disconnect();
            }
            
            
            assertEquals("null", value);
        }
    }
}
