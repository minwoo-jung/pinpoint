package com.navercorp.pinpoint.testweb.service;

import java.net.InetSocketAddress;
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
    private final InetSocketAddress serverAddress = new InetSocketAddress("10.113.168.201", 5000);

    @Override
    public void invoke() {
        try {
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void keepalive() {
        try {
            KeepAliveNpcHessianConnector connector = new KeepAliveNpcHessianConnector(serverAddress);
            invokeAndClose(connector, null);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    @Override
    public void factory() {
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
    public void lightweight() {
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
    public void listener(Runnable callback) {
        try {
            NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);
            invokeAndClose(connector, callback);
        } catch (Exception e) {
            logger.warn("Failed to npc invoke", e);
        }
    }

    private void invokeAndClose(final Invoker invoker, final Runnable callback) throws Exception {
        try {
            InvocationFuture future = invoker.invoke("welcome/test", "hello", "foo");
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
