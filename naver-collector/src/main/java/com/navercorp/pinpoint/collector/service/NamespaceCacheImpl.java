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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
@Profile("tokenAuthentication")
public class NamespaceCacheImpl implements NamespaceCache {

    private final NamespaceService namespaceService;

    @Autowired
    public NamespaceCacheImpl(NamespaceService namespaceService) {
        this.namespaceService = Objects.requireNonNull(namespaceService, "namespaceService must not be null");

    }

    @Cacheable(value = "organizationName", key = "#key")
    @Override
    public String selectPaaSOrganizationkey(String key) {
        PaaSOrganizationKey paaSOrganizationKey = namespaceService.selectPaaSOrganizationkey(key);

        if (paaSOrganizationKey == null) {
            return NOT_EXIST_ORGANIZATION;
        }

        return paaSOrganizationKey.getOrganization();
    }

    @Cacheable(value = "paaSOrganizationInfo", key = "#organizationName")
    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return namespaceService.selectPaaSOrganizationInfo(organizationName);
    }
}
