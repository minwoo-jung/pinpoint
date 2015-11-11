package com.navercorp.pinpoint.testweb.component;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MemcachedClientFactory implements FactoryBean<MemcachedClient>, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String hostAddress;
    private MemcachedClient memcached;


    public MemcachedClientFactory() {
    }

    public MemcachedClientFactory(String hostAddress) {
        Assert.notNull(hostAddress);
        this.hostAddress = hostAddress;
    }

    @Override
    public MemcachedClient getObject() throws Exception {
        return memcached;
    }

    @Override
    public Class<MemcachedClient> getObjectType() {
        return MemcachedClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(hostAddress, "hostPorts");

        logger.info("MemcachedClient init. hostAddress:{}", hostAddress);
        final List<InetSocketAddress> addresses = AddrUtil.getAddresses(hostAddress);
        this.memcached = new MemcachedClient(addresses);
    }

    @Override
    public void destroy() throws Exception {
        if (this.memcached!= null) {
            logger.info("memcached.shutdown()");
            memcached.shutdown();
        }
    }
}
