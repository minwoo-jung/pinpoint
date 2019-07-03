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
import com.navercorp.pinpoint.manager.domain.mysql.metadata.DatabaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.HbaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationKey;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;
import com.navercorp.pinpoint.manager.exception.repository.DuplicateRepositoryException;
import com.navercorp.pinpoint.manager.exception.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Service
@Transactional(transactionManager="metaDataTransactionManager", readOnly = true)
public class MetadataServiceImpl implements MetadataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MetadataDao metadataDao;

    @Autowired
    public MetadataServiceImpl(MetadataDao metadataDao) {
        this.metadataDao = Objects.requireNonNull(metadataDao, "metadataDao must not be null");
    }

    @Override
    public List<RepositoryInfo> getAllRepositoryInfo() {
        return metadataDao.selectAllRepositoryInfo();
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String organizationName) {
        return metadataDao.selectRepositoryInfo(organizationName);
    }

    @Override
    public boolean existOrganization(String organizationName) {
        return metadataDao.existOrganization(organizationName);
    }

    @Override
    public PaaSOrganizationInfo getOrganizationInfo(String organizationName) {
        return metadataDao.selectPaaSOrganizationInfo(organizationName);
    }

    @Override
    public boolean updateOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo) {
        return metadataDao.updatePaaSOrganizationInfo(paaSOrganizationInfo);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public void createOrganization(PaaSOrganizationInfo paaSOrganizationInfo) {
        final String organizationName = paaSOrganizationInfo.getOrganization();
        final String databaseName = paaSOrganizationInfo.getDatabaseName();
        final String hbaseNamespace = paaSOrganizationInfo.getHbaseNamespace();
        try {
            boolean created = metadataDao.insertPaaSOrganizationInfo(paaSOrganizationInfo);
            if (!created) {
                logger.error("Failed to insert PaaSOrganizationInfo : {}", paaSOrganizationInfo);
                throw new RepositoryException(organizationName, "Could not create organization");
            }
        } catch (DuplicateKeyException e) {
            logger.error("Duplicate organizationName : {}", organizationName);
            throw new DuplicateRepositoryException(organizationName);
        }
        insertPaaSOrganizationKey(organizationName);
        initDatabaseStatus(databaseName);
        initHbaseStatus(hbaseNamespace);
    }

    private void insertPaaSOrganizationKey(String organizationName) {
        for (int i = 0 ; i < 10; i++) {
            final String uuid = newUUID();
            boolean existOrganizationKey = metadataDao.existPaaSOrganizationKey(uuid);

            if (existOrganizationKey == false) {
                if (!metadataDao.insertPaaSOrganizationKey(new PaaSOrganizationKey(uuid, organizationName))) {
                    break;
                }
                return;
            }
        }
        logger.error("Failed to generate organization key. organizationName : {}", organizationName);
        throw new RepositoryException(organizationName, "Could not generate organization key");
    }

    private String newUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public void deleteOrganization(PaaSOrganizationInfo paaSOrganizationInfo) {
        final String organizationName = paaSOrganizationInfo.getOrganization();
        final String databaseName = paaSOrganizationInfo.getDatabaseName();
        final String hbaseNamespace = paaSOrganizationInfo.getHbaseNamespace();
        deleteDatabaseStatus(databaseName);
        deleteHbaseStatus(hbaseNamespace);
        metadataDao.deletePaaSOrganizationInfo(organizationName);
        metadataDao.deletePaaSOrganizationKey(organizationName);
    }

    @Override
    public PaaSOrganizationKey getOrganizationKey(String organizationName) {
        return metadataDao.selectPaaSOrganizationKey(organizationName);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public void initDatabaseStatus(String databaseName) {
        DatabaseManagement databaseManagement = new DatabaseManagement();
        databaseManagement.setDatabaseName(databaseName);
        databaseManagement.setDatabaseStatus(StorageStatus.NONE);
        metadataDao.insertDatabaseManagement(databaseManagement);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public boolean deleteDatabaseStatus(String databaseName) {
        return metadataDao.deleteDatabaseManagement(databaseName);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public void initHbaseStatus(String hbaseNamespace) {
        HbaseManagement hbaseManagement = new HbaseManagement();
        hbaseManagement.setHbaseNamespace(hbaseNamespace);
        hbaseManagement.setHbaseStatus(StorageStatus.NONE);
        metadataDao.insertHbaseManagement(hbaseManagement);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public boolean deleteHbaseStatus(String hbaseNamespace) {
        return metadataDao.deleteHbaseManagement(hbaseNamespace);
    }
}
