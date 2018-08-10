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
package com.navercorp.pinpoint.plugin.tomcat;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.navercorp.pinpoint.plugin.NaverAgentPath;
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
@PinpointAgent(NaverAgentPath.PATH)
@JvmVersion(7)
@TraceObjectManagable
public class TomcatIT {
    private static final String TOMCAT = "TOMCAT";

    @Test
    public void testServerType() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.verifyServerType(TOMCAT);
        verifier.verifyServerInfo(ServerInfo.getServerInfo());
        verifier.verifyConnector("HTTP/1.1", 8972);
        verifier.verifyService("Catalina/localhost/test", Arrays.asList("hamcrest-core-1.3.jar", "junit-4.12.jar", "pinpoint-test-" + Version.VERSION + ".jar"));
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
}
