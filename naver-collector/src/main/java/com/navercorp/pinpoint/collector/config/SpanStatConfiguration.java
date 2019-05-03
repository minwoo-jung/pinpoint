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
package com.navercorp.pinpoint.collector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class SpanStatConfiguration extends FlinkConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpanStatConfiguration.class);

    @Override
    protected void readPropertyValues(Properties properties) {
        LOGGER.info("pinpoint-collector.properties read.");

        this.flinkClusterEnable = CollectorConfiguration.readBoolean(properties, "span.stat.flink.cluster.enable");
        this.flinkClusterZookeeperAddress = CollectorConfiguration.readString(properties, "span.stat.flink.cluster.zookeeper.address", "");
        this.flinkClusterSessionTimeout = CollectorConfiguration.readInt(properties, "span.stat.flink.cluster.zookeeper.sessiontimeout", -1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanStatConfiguration{");
        sb.append("flinkClusterEnable=").append(flinkClusterEnable);
        sb.append(", flinkClusterZookeeperAddress='").append(flinkClusterZookeeperAddress).append('\'');
        sb.append(", flinkClusterSessionTimeout=").append(flinkClusterSessionTimeout);
        sb.append('}');
        return sb.toString();
    }
}
