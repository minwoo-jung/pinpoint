package com.navercorp.test.pinpoint.testweb.component;
/*
 * Copyright 2019 NAVER Corp.
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
 */

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Arrays;

public class MongoClientFactory37 implements FactoryBean<MongoClient>, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String hostAddress;
    private int hostPort;
    private MongoClient mongoClient;

    public MongoClientFactory37() {
    }

    public MongoClientFactory37(String hostAddress, int hostPort) {
        Assert.notNull(hostAddress, "hostAddress must not be null");
        Assert.notNull(hostPort, "hostPort must not be null");
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() {
        if (this.mongoClient != null) {
            logger.info("mongoClient close()");
            mongoClient.close();
        }
    }

    @Override
    public MongoClient getObject() {
        return mongoClient;
    }

    @Override
    public Class<MongoClient> getObjectType() {
        return MongoClient.class;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(hostAddress, "hostAddress");
        Assert.notNull(hostPort, "hostPorts");

        logger.info("MongoClient init. hostAddress:{} hostPorts:{}", hostAddress, hostPort);
        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder().readPreference(ReadPreference.secondary())
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(hostAddress, hostPort)))).build());
    }
}
