package com.navercorp.pinpoint.testweb.component;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArcusClientFactory implements FactoryBean<ArcusClient>, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ArcusClient arcusClient;
    private String hostPorts;
    private String serviceCode;

    public ArcusClientFactory(String hostPorts, String serviceCode) {
        Assert.notNull(hostPorts, "hostPorts");
        Assert.notNull(serviceCode, "serviceCode");
        this.hostPorts = hostPorts;
        this.serviceCode = serviceCode;
    }


    @Override
    public ArcusClient getObject() throws BeansException {
        return arcusClient;
    }

    @Override
    public Class<?> getObjectType() {
        return ArcusClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(hostPorts, "hostPorts");
        Assert.notNull(serviceCode,  "serviceCode");

        logger.info("ArcusClient init. hostPorts:{} serviceCode:{}", hostPorts, serviceCode);
        final ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
        this.arcusClient = ArcusClient.createArcusClient(hostPorts, serviceCode, cfb);
    }


    @Override
    public void destroy() throws Exception {
        if (this.arcusClient!= null) {
            logger.info("arcusClient.shutdown()");
            arcusClient.shutdown();
        }
    }
}
