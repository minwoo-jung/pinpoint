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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;
import com.nhncorp.lucy.bloc.core.Container;
import com.nhncorp.lucy.bloc.core.conf.Configuration;
import com.nhncorp.lucy.bloc.core.test.TestContainer;
import com.nhncorp.lucy.bloc.http.HttpConfiguration;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmVersion(7)
@JvmArgument({"-Dbloc.home=.", "-Dbloc.base=."})
@Repository("http://repo.nhncorp.com/maven2")
@Dependency("com.nhncorp.lucy:bloc-server:[4,4.0.3]")
@TraceObjectManagable
public class BlocIT {
    private static final int HTTP_PORT = 5111;
    private static Container container;
    
    private static final String BLOC = "BLOC";
    
    @BeforeClass
    public static void startBloc() {
        Configuration config = new Configuration();
        config.set(HttpConfiguration.ADDRESS, "*:" + HTTP_PORT);
        
        container = TestContainer.of("com.navercorp.pinpoint.plugin.bloc.v4.module", config);
        container.start();
    }
    
    @AfterClass
    public static void stopBloc() {
        container.stop();
    }
    
    @Test
    @Ignore("BLOC 프로세스를 직접 띄우는 형태로 변경할 때까지")
    public void testServerType() {
        PluginTestVerifier agent = PluginTestVerifierHolder.getInstance();
        agent.verifyServerType(BLOC);
    }
    
    @Test
    public void testInvocation() throws Exception {
        String path = "/module/hello/sayHello";
        String queryString = "name=pinpoint";
        String pathWithQueryString = path + "?" + queryString;
        URL url = new URL("http://localhost:" + HTTP_PORT + pathWithQueryString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();
        
        PluginTestVerifier agent = PluginTestVerifierHolder.getInstance();
        
        agent.verifySpanCount(2);
        agent.verifySpanEvent(ServiceType.INTERNAL_METHOD.getName(),
                annotation("CALL_URL", path),
                annotation("PROTOCOL", "http"));
        agent.verifySpan(BLOC,
                annotation("http.url", pathWithQueryString),
                annotation("http.param", queryString));
    }
}
