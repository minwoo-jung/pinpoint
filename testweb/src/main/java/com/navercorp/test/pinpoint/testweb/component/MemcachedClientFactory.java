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

package com.navercorp.test.pinpoint.testweb.component;

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
