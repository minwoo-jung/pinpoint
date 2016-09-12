/*
 * Copyright 2016 NAVER Corp.
 *
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
 *
 */

package com.navercorp.test.pinpoint.testweb.npc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class NPCTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void connect() {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("0.0.0.0", 5000);
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();

            logger.debug("{}", result);
            Assert.assertNotNull(result);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
