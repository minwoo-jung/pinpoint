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
package com.navercorp.test.pinpoint.plugin.bloc.v4;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;

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
    private static final String BLOC_INTERNAL_METHOD = "BLOC_INTERNAL_METHOD";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

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
        
        // give chance to run interceptor's after method. sometimes the response arrives before the method is excuted. 
        Thread.sleep(100);
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR", "JACKSON");

        verifier.verifyTrace(root(BLOC, "BLOC Business Logic Process", pathWithQueryString, "127.0.0.1:" + HTTP_PORT, "127.0.0.1", annotation("http.url", pathWithQueryString)));
        verifier.verifyTrace(event(BLOC_INTERNAL_METHOD, "com.nhncorp.lucy.bloc.http.NettyInboundHandler.channelRead0(io.netty.channel.ChannelHandlerContext, io.netty.handler.codec.http.FullHttpRequest)", annotation("http.param", queryString)));
        verifier.verifyTrace(event(INTERNAL_METHOD, "com.nhncorp.lucy.bloc.core.processor.RequestProcessor.process(com.nhncorp.lucy.bloc.core.processor.BlocRequest)", annotation("CALL_URL", path), annotation("PROTOCOL", "http")));

        verifier.verifyTraceCount(0);
    }
}
