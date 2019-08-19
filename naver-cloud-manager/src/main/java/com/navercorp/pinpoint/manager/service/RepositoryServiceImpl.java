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
package com.navercorp.pinpoint.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author minwoo.jung
 */

@Service
public class RepositoryServiceImpl implements RepositoryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MetadataService metadataService;

    @Autowired
    LiquibaseService liquibaseService;

    @Autowired
    HbaseRepositoryService hbaseRepositoryService;

    @Override
    public void createRepository(String organizationName, String userId) throws Exception {
        metadataService.createOrganizationInfo(organizationName);

        try {
            liquibaseService.createRepository(organizationName, userId);
        } catch (Exception e) {
            logger.error("can not create repository", e);
            metadataService.dropDatabaseAndDeleteOrganizationInfo(organizationName);
            throw e;
        }

        try {
            hbaseRepositoryService.createRepository(organizationName, userId);
        } catch (Exception e) {
            metadataService.dropDatabaseAndDeleteOrganizationInfo(organizationName);
            hbaseRepositoryService.dropTable(organizationName);
            throw e;
        }
    }
}
