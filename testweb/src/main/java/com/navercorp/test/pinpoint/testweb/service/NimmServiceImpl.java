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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.net.invoker.InvocationFutureListener;
import com.nhncorp.lucy.nimm.connector.NimmConnector;
import com.nhncorp.lucy.nimm.connector.NimmSocket;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.bloc.NimmInvoker;

@Service("nimmService")
public class NimmServiceImpl implements NimmService {
    private static final String NIMM_CONFIG_FILE = "/NimmConnector.xml";
    private static final String OBJECT_NAME = "welcome/com.nhncorp.lucy.bloc.welcome.EchoBO";
    private static final String METHOD_NAME = "execute";
    private static final int DOMAIN_ID = 12371;
    private static final int IDC_ID = 12;
    private static final int SERVER_ID = 8742;
    private static final int SOCKET_ID = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private NimmSocket localSocket;
    private Map<String, NimmInvoker> invokers = new HashMap<String, NimmInvoker>();

    @PostConstruct
    public void init() throws Exception {
        NimmConnector.registerMMNetDriver(NIMM_CONFIG_FILE);
        NimmAddress bloc3Address = NimmAddress.createUnicastAddress(12372, 12, 22799, 1);
        NimmAddress bloc4Address = NimmAddress.createUnicastAddress(12372, 12, 60022, 1);
        this.localSocket = NimmConnector.createNimmSocket();

        invokers.put("bloc3", new NimmInvoker(bloc3Address, localSocket, 1000));
        invokers.put("bloc4", new NimmInvoker(bloc4Address, localSocket, 1000));
    }

    @PreDestroy
    public void destroy() {
        NimmConnector.shutdownGracefully();
    }

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    public void get(String address) {
        invoke(address, null);
    }


    @Override
    public void get(String address, Runnable callback) {
        invoke(address, callback);
    }

    private void invoke(String address, final Runnable callback) {
        NimmInvoker invoker = invokers.get(address);
        if(invoker == null) {
            throw new IllegalArgumentException("not found nimm invoker " + address);
        }

        try {
            logger.info("Invoke {}.{}", OBJECT_NAME, METHOD_NAME);

            Map<String, String> params = new HashMap<String, String>();
            params.put("foo", "bar");
            final InvocationFuture future = invoker.invoke(OBJECT_NAME, METHOD_NAME, params);
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
                final CountDownLatch latch = new CountDownLatch(1);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            future.await();
                            Object returnValue = future.getReturnValue();
                            logger.info("Return {}", returnValue);
                        } catch (Exception ignored) {
                        }
                        latch.countDown();
                    }
                });
                thread.start();
                latch.await(2000L, TimeUnit.MICROSECONDS);
            }
        } catch (Exception e) {
            logger.warn("Failed to nimm invoke", e);
        }
    }
}
