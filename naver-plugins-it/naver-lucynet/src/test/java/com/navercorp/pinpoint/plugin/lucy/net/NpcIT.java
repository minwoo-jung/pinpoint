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
package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.pluginit.utils.NaverAgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.net.call.DefaultReturnValue;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.invoker.DefaultInvocationFuture;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.acceptor.NpcHessianExtendedAcceptor;
import com.nhncorp.lucy.npc.acceptor.NpcHessianMessageHandler;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.async;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Jongho Moon
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(NaverAgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"com.nhncorp.lucy:lucy-npc:[1.5.18,)"})
public class NpcIT {
    private static final String NPC = "NPC_CLIENT";
    private static final String NPC_CLIENT_INTERNAL = "NPC_CLIENT_INTERNAL";

    private static final String SERVER_IP = "0.0.0.0";
    private static final int SERVER_PORT = 5917;
    private static final String DESTINATION_ID = SERVER_IP + ":" + SERVER_PORT;
    private static final String DESTINATION_ID_2 = SERVER_IP + "/" + SERVER_IP + ":" + SERVER_PORT;


    private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(SERVER_IP, SERVER_PORT);
    private static NpcHessianExtendedAcceptor acceptor;

    private static class ExtendedAcceptor implements NpcHessianMessageHandler {
        public Reply handleMessage(Call call, Charset charSet) {
            return new DefaultReturnValue(call.getParameters().get(0));
        }
    }

    @BeforeClass
    public static void startServer() throws Exception {
        acceptor = new NpcHessianExtendedAcceptor(SERVER_ADDRESS, new ExtendedAcceptor());
        acceptor.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        acceptor.stop();
    }

    @Test
    public void test() throws Exception {
        NpcHessianConnector npcHessianConnector = new NpcHessianConnector(SERVER_ADDRESS, true);
        InvocationFuture future = npcHessianConnector.invoke(null, "echo", "Hello");
        future.await();
        String response = future.get();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method createConnector = NpcHessianConnector.class.getDeclaredMethod("createConnecor", NpcConnectorOption.class);

        Class<?> nioNpcHessianConnector = Class.forName("com.nhncorp.lucy.npc.connector.NioNpcHessianConnector");
        Constructor<?> nioNpcHessianConnectorConstructor = nioNpcHessianConnector.getDeclaredConstructor(NpcConnectorOption.class);

        Method npcHessianConnectorInvoke = NpcHessianConnector.class.getDeclaredMethod("invoke", String.class, String.class, Object[].class);
        Method invoke = nioNpcHessianConnector.getDeclaredMethod("invoke", String.class, String.class, Charset.class, Object[].class);
        Method isReadAndSet = DefaultInvocationFuture.class.getMethod("isReadyAndSet", Object.class);
        Method get = DefaultInvocationFuture.class.getDeclaredMethod("get");
        Method getReturnValue = DefaultInvocationFuture.class.getDeclaredMethod("getReturnValue");

        verifier.verifyTrace(event(NPC_CLIENT_INTERNAL, createConnector, annotation("npc.url", DESTINATION_ID)));
        verifier.verifyTrace(event(NPC_CLIENT_INTERNAL, nioNpcHessianConnectorConstructor, annotation("npc.url", DESTINATION_ID)));
        verifier.verifyTrace(event(NPC_CLIENT_INTERNAL, npcHessianConnectorInvoke));
        verifier.verifyTrace(async(event(NPC, invoke, null, null, DESTINATION_ID, annotation("npc.url", SERVER_ADDRESS.toString())),
                event("ASYNC", "Asynchronous Invocation"),
                event(ServiceType.INTERNAL_METHOD.getName(), isReadAndSet)));
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), get));
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), getReturnValue));
    }

}
