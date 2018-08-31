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
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Taejin Koo
 */
@Repository
@Profile("tokenAuthentication")
public class MemoryMetadataDao implements MetadataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final Map<String, PaaSOrganizationInfo> paaSOrganizationInfoMap = new HashMap<>();
    public final Map<String, PaaSOrganizationKey> paaSOrganizationKeyMap = new HashMap<>();

    @PostConstruct
    public void setup() {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource("metadata_dummy.txt").toURI());
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                if (StringUtils.hasText(line)) {
                    String[] metadatas = line.split(",");
                    if (ArrayUtils.getLength(metadatas) == 4) {
                        createMetadata(metadatas);
                    }
                }
            }
        } catch (Exception e) {
            logger.info("failed while creating mock metadata");
        }
    }

    private void createMetadata(String[] metadatas) {
        String licenseKey = metadatas[0];
        String organization = metadatas[1];
        String databaseName = metadatas[2];
        String hbaseNameSpaceName = metadatas[3];

        if (StringUtils.hasText(licenseKey) && StringUtils.hasText(organization) && StringUtils.hasText(databaseName) && StringUtils.hasText(hbaseNameSpaceName)) {
            createPaaSOrganizationkey(licenseKey, new PaaSOrganizationKey(UUID.randomUUID().toString(), organization));
            createPaaSOrganizationInfo(organization, new PaaSOrganizationInfo(organization, databaseName, hbaseNameSpaceName));
        }
    }

    private boolean createPaaSOrganizationInfo(String organizationName, PaaSOrganizationInfo paaSOrganizationInfo) {
        PaaSOrganizationInfo oldValue = paaSOrganizationInfoMap.putIfAbsent(organizationName, paaSOrganizationInfo);
        return oldValue == null;
    }

    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return paaSOrganizationInfoMap.get(organizationName);
    }

    private boolean createPaaSOrganizationkey(String licenseKey, PaaSOrganizationKey paaSOrganizationKey) {
        PaaSOrganizationKey oldValue = paaSOrganizationKeyMap.putIfAbsent(licenseKey, paaSOrganizationKey);
        return oldValue == null;
    }

    @Override
    public PaaSOrganizationKey selectPaaSOrganizationkey(String licenseKey) {
        return paaSOrganizationKeyMap.get(licenseKey);
    }

}
