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
package com.navercorp.pinpoint.plugin.arcus;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@JvmArgument({"-Dbloc.home=.", "-Dbloc.base=."})
@Repository("http://repo.nhncorp.com/maven2")
@Dependency({"arcus:arcus-client:1.5.3", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class ArcusTest {
    private static HelloArcus helloArcus;

    @BeforeClass
    public static void beforeClass() {
        helloArcus = new HelloArcus("ncloud.arcuscloud.nhncorp.com:17288", "ff31ddb85e9b431c8c0e5e50a4315c27");
    }
    
    @Test
    public void doTest() throws Exception {
        helloArcus.sayHello();
        helloArcus.listenHello();
        helloArcus.listenHello();
        
        Method set = MemcachedClient.class.getMethod("set", String.class, int.class, Object.class);
        Method operationFutureGet = OperationFuture.class.getMethod("get", long.class, TimeUnit.class);
        Method asyncGet = MemcachedClient.class.getMethod("asyncGet", String.class);
        Method getFutureGet = GetFuture.class.getMethod("get", long.class, TimeUnit.class);
        
        String key = "test:hello";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.verifyApi(ServiceType.ARCUS, set, key);
        verifier.verifyApi(ServiceType.ARCUS_FUTURE_GET, operationFutureGet);
        verifier.verifyApi(ServiceType.ARCUS, asyncGet, key);
        verifier.verifyApi(ServiceType.ARCUS_FUTURE_GET, getFutureGet);
        verifier.verifyApi(ServiceType.ARCUS, asyncGet, key);
        verifier.verifySpanCount(0);
        
        verifier.printApis(System.out);
    }
}
