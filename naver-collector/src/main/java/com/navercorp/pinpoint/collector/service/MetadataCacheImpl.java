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
import org.springframework.stereotype.Service;

/**
 * @author minwoo.jung
 */
@Service
public class MetadataCacheImpl implements MetadataCache {

    @Autowired
    MetadataService metadataService;

    @Cacheable(value = "organizationName", key = "#key")
    @Override
    public String selectPaaSOrganizationkey(String key) {
        PaaSOrganizationKey paaSOrganizationKey = metadataService.selectPaaSOrganizationkey(key);

        if (paaSOrganizationKey == null) {
            return NOT_EXIST_ORGANIZATION;
        }

        return paaSOrganizationKey.getOrganization();
    }

    @Cacheable(value = "paaSOrganizationInfo", key = "#organizationName")
    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return metadataService.selectPaaSOrganizationInfo(organizationName);
    }
}
