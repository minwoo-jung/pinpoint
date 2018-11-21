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
package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.MetaDataService;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author minwoo.jung
 */
public class ApplicationControllerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{pinpointWebProps['namespaceInfo.defaultValue.userId']}")
    private String userId;

    @Value("#{pinpointWebProps['namespaceInfo.defaultValue.organizationName']}")
    private String organizationName;

    @Autowired
    MetaDataService metaDataService;

    public void beforeIntercept(JoinPoint joinPoint) throws Throwable {
        boolean allocateSuccess = metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName);

        if (allocateSuccess == false) {
            String message = String.format("error occurred in allocatePaaSOrganizationInfo userId(%s), organizationName(%s).", userId, organizationName);
            logger.error(message);
            throw new RuntimeException(message);
        }
    }
}