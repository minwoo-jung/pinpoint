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
package com.navercorp.pinpoint.collector.sender;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestAttributes;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkRequestFactoryInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void beforeIntercept(JoinPoint joinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("start setting namespaceInfo in Header from RequestContextHolder.");
        }
        Object[] args = joinPoint.getArgs();
        Map<String, String> headerEntity = (Map<String, String>) args[1];
        if (headerEntity == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("not set namespaceInfo in Header from RequestContextHolder. Because headerEntity is null.");
            }
        }

        RequestAttributes attributes = RequestContextHolder.currentAttributes();
        NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) attributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
        Objects.requireNonNull(nameSpaceInfo, "NameSpaceInfo");

        headerEntity.put("organization", nameSpaceInfo.getOrganization());
        headerEntity.put("databaseName", nameSpaceInfo.getMysqlDatabaseName());
        headerEntity.put("hbaseNameSpace", nameSpaceInfo.getHbaseNamespace());

        if (logger.isDebugEnabled()) {
            logger.debug("end setting namespaceInfo in Header from RequestContextHolder. namespaceInfo : {}", nameSpaceInfo);
        }
    }

}
