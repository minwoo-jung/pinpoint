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

package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
@Service
public class NamespaceGenerationServiceImpl implements NamespaceGenerationService {

    @Override
    public String gernerateName(String organizationName) {
        final int length = organizationName.length();
        final int requiredLength= PaaSOrganizationInfo.DATABASE_NAME_MAX_SIZE - length;

        final String uuid = UUID.randomUUID().toString().replace("-", "");
        String substring = uuid.substring(0, requiredLength-1);
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(organizationName).append(PaaSOrganizationInfo.DATABASE_NAME_DELIMITER).append(substring);
        return nameBuilder.toString();



    }

    @Deprecated
    @Override
    public String generateDatabaseName(String organizationName) {
        // TODO implement logic, check for availability and retry
        return organizationName;
    }

    @Deprecated
    @Override
    public String generateHbaseNamespace(String organizationName) {
        // TODO implement logic, check for availability and retry
        return organizationName;
    }
}
