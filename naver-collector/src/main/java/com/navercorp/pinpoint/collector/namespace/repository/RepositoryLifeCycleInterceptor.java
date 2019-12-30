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
package com.navercorp.pinpoint.collector.namespace.repository;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class RepositoryLifeCycleInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NamespaceService namespaceService;
    private volatile Map<String, Boolean> organizationLifeCycle = new ConcurrentHashMap<>();

    public RepositoryLifeCycleInterceptor(NamespaceService namespaceService) {
        Objects.requireNonNull(namespaceService, "namespaceService");
        this.namespaceService = namespaceService;
    }

    @PostConstruct
    public void postConstruct() {
        updateOrganizationLifeCycle();
    }

    public void aroundAdvice(ProceedingJoinPoint joinPoint, ServerRequest serverRequest) throws Throwable {
        NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) serverRequest.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
        String organizationName = nameSpaceInfo.getOrganization();

        Boolean enable = organizationLifeCycle.get(organizationName);

        if (Boolean.FALSE.equals(enable)) {
            return;
        }

        joinPoint.proceed();
    }

    private void replaceOrganizationLifeCycle(Map<String, Boolean> organizationLifeCycle) {
        this.organizationLifeCycle = organizationLifeCycle;
    }

    public void updateOrganizationLifeCycle() {
        List<PaaSOrganizationLifeCycle> PaaSOrganizationLifeCycleList = namespaceService.selectPaaSOrganizationLifeCycle();

        Map<String, Boolean> organizationLifeCycleMap = new ConcurrentHashMap<>();

        for (PaaSOrganizationLifeCycle organizationLifeCycle : PaaSOrganizationLifeCycleList) {
            boolean enable = organizationLifeCycle.isEnable();

            if (enable == false) {
                organizationLifeCycleMap.put(organizationLifeCycle.getOrganization(), new Boolean(enable));
            }
        }

        logger.info("replace OrganizationLifeCycle data : " + organizationLifeCycleMap);

        replaceOrganizationLifeCycle(organizationLifeCycleMap);
    }
}
