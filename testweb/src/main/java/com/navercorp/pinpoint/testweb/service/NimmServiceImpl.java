package com.navercorp.pinpoint.testweb.service;

import java.util.concurrent.CountDownLatch;

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
    private static final String OBJECT_NAME = "welcome/test";
    private static final String METHOD_NAME = "hello";
    private static final int DOMAIN_ID = 12371;
    private static final int IDC_ID = 12;
    private static final int SERVER_ID = 32028;
    private static final int SOCKET_ID = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void get() {
        invoke(null);
    }

    @Override
    public void get(Runnable callback) {
        invoke(callback);
    }
    
    private void invoke(final Runnable callback) {
        try {
            logger.info("Register NIMM. config={}, bloc={}:{}:{}:{}", NIMM_CONFIG_FILE, DOMAIN_ID, IDC_ID, SERVER_ID, SOCKET_ID);
            NimmConnector.registerMMNetDriver(NIMM_CONFIG_FILE);
            NimmAddress blocAddress = NimmAddress.createUnicastAddress(DOMAIN_ID, IDC_ID, SERVER_ID, SOCKET_ID);
            NimmSocket localSocket = NimmConnector.createNimmSocket();

            NimmInvoker nimmInvoker = new NimmInvoker(blocAddress, localSocket, 1000);
            logger.info("Invoke {}.{}", OBJECT_NAME, METHOD_NAME);
            InvocationFuture future = nimmInvoker.invoke(OBJECT_NAME, METHOD_NAME, "foo");
            if(callback != null) {
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
                Object returnValue = future.getReturnValue();
                logger.info("Return {}", returnValue);
            }
        } catch (Exception e) {
            logger.warn("Failed to nimm invoke", e);
        } finally {
            logger.info("Shutdown NIMM.");
            NimmConnector.shutdownGracefully();
        }
    }
}
