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
package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.manager.vo.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(transactionManager="metaDataTransactionManager", rollbackFor = {Exception.class})
public class MetaDataServiceImpl implements MetadataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MetadataDao metadataDao;

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public boolean existOrganization(String organizationName) {
        return metadataDao.existOrganization(organizationName);
    }

    @Override
    public void createOrganizationInfo(String organizationName) {
        createDatabase(organizationName);
        insertOrganizationInfo(organizationName);
        insertPaaSOrganizationKey(organizationName);
    }

    private void insertOrganizationInfo(String organizationName) {
        if (!metadataDao.insertPaaSOrganizationInfo(organizationName)) {
            String message = "can not insert data for OrganizationInfo. organizationName :" + organizationName;
            logger.error(message);
            throw new RepositoryException(message);
        }
    }


    private void createDatabase(String organizationName) {
        if (!metadataDao.createDatabase(organizationName)) {
            String message = "can not create database. organizationName :" + organizationName;
            logger.error(message);
            throw new RepositoryException(message);
        };
    }

    private void insertPaaSOrganizationKey(String organizationName) {
        for (int i=0 ; i < 10; i++) {
            final String uuid = newUUID();
            boolean existOrganizationKey = metadataDao.existPaaSOrganizationKey(uuid);

            if (existOrganizationKey == false) {
                if (!metadataDao.insertPaaSOrganizationKey(new PaaSOrganizationKey(uuid, organizationName))) {
                    break;
                }
                return;
            }
        }

        String message = "can not generate organization key. organizationName :" + organizationName;
        logger.error(message);
        throw new RepositoryException(message);
    }

    private String newUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    @Override
    public void dropDatabaseAndDeleteOrganizationInfo(String organizationName) {
        metadataDao.deletePaaSOrganizationInfo(organizationName);
        metadataDao.deletePaaSOrganizationKey(organizationName);
        metadataDao.dropDatabase(organizationName);
    }
}
