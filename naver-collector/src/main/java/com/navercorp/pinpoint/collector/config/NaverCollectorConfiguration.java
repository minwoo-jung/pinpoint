/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class NaverCollectorConfiguration extends CollectorConfiguration {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean flinkClusterEnable;
    private String flinkClusterZookeeperAddress;
    private int flinkClusterSessionTimeout;

    public boolean isFlinkClusterEnable() {
        return flinkClusterEnable;
    }

    public String getFlinkClusterZookeeperAddress() {
        return flinkClusterZookeeperAddress;
    }

    public int getFlinkClusterSessionTimeout() {
        return flinkClusterSessionTimeout;
    }

    @Override
    protected  void readPropertyValues(Properties properties) {
        logger.info("pinpoint-collector.properties read.");
        this.flinkClusterEnable = readBoolean(properties, "flink.cluster.enable");
        this.flinkClusterZookeeperAddress = readString(properties, "flink.cluster.zookeeper.address", "");
        this.flinkClusterSessionTimeout = readInt(properties, "flink.cluster.zookeeper.sessiontimeout", -1);
    }
}
