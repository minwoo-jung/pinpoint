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

package com.navercorp.test.pinpoint.testweb.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.net.invoker.InvocationFutureListener;
import com.nhncorp.lucy.net.invoker.Invoker;
import com.nhncorp.lucy.npc.connector.ConnectionFactory;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

@Service("npcService")
public class NpcServiceImpl implements NpcService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void invoke(InetSocketAddress serverAddress) {
        try {
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void keepalive(InetSocketAddress serverAddress) {
        try {
            KeepAliveNpcHessianConnector connector = new KeepAliveNpcHessianConnector(serverAddress);
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void factory(InetSocketAddress serverAddress) {
        try {
            ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

            npcConnectionFactory.setTimeout(1000L);
            npcConnectionFactory.setAddress(serverAddress);

            NpcHessianConnector connector = npcConnectionFactory.create();
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void lightweight(InetSocketAddress serverAddress) {
        try {
            ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

            npcConnectionFactory.setTimeout(1000L);
            npcConnectionFactory.setAddress(serverAddress);
            npcConnectionFactory.setLightWeight(true);

            NpcHessianConnector connector = npcConnectionFactory.create();
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void listener(InetSocketAddress serverAddress, Runnable callback) {
        try {
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);
            invokeAndClose(connector, callback);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    private void invokeAndClose(final Invoker invoker, final Runnable callback) throws Exception {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("foo", "bar");
            InvocationFuture future = invoker.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);
            if (callback != null) {
                final CountDownLatch latch = new CountDownLatch(1);
                future.addListener(new InvocationFutureListener() {
                    @Override
                    public void invocationComplete(InvocationFuture future) throws Exception {
                        logger.info("Return {}", future.getReturnValue());
                        callback.run();
                        latch.countDown();
                    }
                });
                latch.await();
            } else {
                future.await();
                Object result = future.getReturnValue();
                logger.info("Return {}", result);
            }
        } finally {
            if (invoker != null) {
                invoker.dispose();
            }
        }
    }
}
