/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.memory;

import com.navercorp.pinpoint.collector.dao.MetadataDao;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@Repository
@Profile("tokenAuthentication")
public class MemoryMetadataDao implements MetadataDao {

    public final Map<String, PaaSOrganizationInfo> paaSOrganizationInfoMap = new HashMap<>();
    public final Map<String, PaaSOrganizationKey> paaSOrganizationKeyMap = new HashMap<>();

    public boolean createPaaSOrganizationInfo(String organizationName, PaaSOrganizationInfo paaSOrganizationInfo) {
        PaaSOrganizationInfo oldValue = paaSOrganizationInfoMap.putIfAbsent(organizationName, paaSOrganizationInfo);
        return oldValue == null;
    }

    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return paaSOrganizationInfoMap.get(organizationName);
    }

    public boolean createPaaSOrganizationkey(String licenseKey, PaaSOrganizationKey paaSOrganizationKey) {
        PaaSOrganizationKey oldValue = paaSOrganizationKeyMap.putIfAbsent(licenseKey, paaSOrganizationKey);
        return oldValue == null;
    }

    @Override
    public PaaSOrganizationKey selectPaaSOrganizationkey(String licenseKey) {
        return paaSOrganizationKeyMap.get(licenseKey);
    }

}
