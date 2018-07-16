/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.flink.namespace;

import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public abstract class FlinkContextInterceptor {

    protected static final String TUPLE_KEY_DELIMITER = "@@";
    protected static final String FLINK = "flink";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void initFlinkcontextHolder(PaaSOrganizationInfo paaSOrganizationInfo) {
        if (paaSOrganizationInfo == null) {
            logger.debug("initFlinkcontextHolder - paaSOrganizationInfo is null");
            return;
        }

        FlinkAttributes flinkAttributes  = new FlinkAttributes(new ConcurrentHashMap<>());
        flinkAttributes.setAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
        FlinkContextHolder.setAttributes(flinkAttributes);

        if (logger.isDebugEnabled()) {
            logger.debug("initFlinkContextHolder paaSOrganizationInfo : {}", paaSOrganizationInfo);
        }
    }

    protected void resetFlinkcontextHolder() {
        FlinkContextHolder.resetAttributes();
    }
}
