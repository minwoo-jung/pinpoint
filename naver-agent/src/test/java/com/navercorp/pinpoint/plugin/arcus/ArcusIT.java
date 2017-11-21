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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.plugin.FrontCacheGetFuture;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("naver-agent/target/pinpoint-naver-agent-" + Version.VERSION)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"arcus:arcus-client:[1.5.4],[1.6.0],[1.6.4,)", "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5"})
public class ArcusIT {
    private static final String KEY = "test:hello";

    private static ArcusClient arcusClient;

    private final static String ARCUS = "ARCUS";
    private final static String ARCUS_FUTURE_GET = "ARCUS_FUTURE_GET";
    private final static String ARCUS_EHCACHE_FUTURE_GET = "ARCUS_EHCACHE_FUTURE_GET";

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");

        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        builder.setFrontCacheExpireTime(10000);
        builder.setMaxFrontCacheElements(100);
        arcusClient = ArcusClient.createArcusClient("kra-dev.arcuscloud.navercorp.com:17288", "dev_pinpoint", builder);
    }

    public boolean set() throws Exception {
        Future<Boolean> future = arcusClient.set(KEY, 600, "Hello, Arcus!");

        try {
            return future.get(700L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw e;
        }
    }

    public String asyncGet() throws Exception {
        Future<Object> future = arcusClient.asyncGet(KEY);

        try {
            return (String)future.get(3000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (future != null) future.cancel(true);
            throw e;
        }
    }
    
    public String get() throws Exception {
        return (String)arcusClient.get(KEY);
    }    
    
    @Test
    public void doTest() throws Exception {
        Method set = MemcachedClient.class.getMethod("set", String.class, int.class, Object.class);
        Method asyncGet = MemcachedClient.class.getMethod("asyncGet", String.class);
        Method get = MemcachedClient.class.getMethod("get", String.class);
        Method operationFutureGet = OperationFuture.class.getMethod("get", long.class, TimeUnit.class);
        Method getFutureGet = GetFuture.class.getMethod("get", long.class, TimeUnit.class);
        Method getFutureSet = GetFuture.class.getMethod("set", Future.class);
        Method frontCacheFutureGet = FrontCacheGetFuture.class.getMethod("get", long.class, TimeUnit.class);
        
        
        set();
        asyncGet();
        asyncGet();
        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();

        verifier.verifyTrace(event(ARCUS, set, args(KEY)),
                event(ARCUS_FUTURE_GET, operationFutureGet));
        
        verifier.verifyTrace(async(event(ARCUS, asyncGet, args(KEY)), 
                                        event("ASYNC", "Asynchronous Invocation"),
                                        event(ARCUS_FUTURE_GET, getFutureSet)));
        
        
        verifier.verifyTrace(event(ARCUS_FUTURE_GET, getFutureGet));
        verifier.verifyTrace(event(ARCUS, asyncGet, args(KEY)));
        verifier.verifyTrace(event(ARCUS_EHCACHE_FUTURE_GET, frontCacheFutureGet));

        get();
        
        verifier.verifyTrace(event(ARCUS, get, args(KEY)));
    }
}
