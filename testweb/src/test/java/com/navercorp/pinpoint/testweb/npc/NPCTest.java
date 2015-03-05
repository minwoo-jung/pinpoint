package com.navercorp.pinpoint.testweb.npc;

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
