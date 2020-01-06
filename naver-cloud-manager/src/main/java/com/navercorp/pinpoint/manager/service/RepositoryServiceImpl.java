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

import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;
import com.navercorp.pinpoint.manager.exception.database.DatabaseManagementException;
import com.navercorp.pinpoint.manager.exception.hbase.HbaseManagementException;
import com.navercorp.pinpoint.manager.exception.repository.InvalidRepositoryStateException;
import com.navercorp.pinpoint.manager.exception.repository.RepositoryException;
import com.navercorp.pinpoint.manager.exception.repository.UnknownRepositoryException;
import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoDetail;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoBasic;
import com.navercorp.pinpoint.manager.vo.user.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */

@Service
public class RepositoryServiceImpl implements RepositoryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MetadataService metadataService;

    private final DatabaseManagementService databaseManagementService;

    private final HbaseManagementService hbaseManagementService;

    private final NamespaceGenerationService namespaceGenerationService;

    private final PinpointDataService pinpointDataService;

    @Autowired
    public RepositoryServiceImpl(MetadataService metadataService,
                                 DatabaseManagementService databaseManagementService,
                                 HbaseManagementService hbaseManagementService,
                                 NamespaceGenerationService namespaceGenerationService,
                                 PinpointDataService pinpointDataService) {
        this.metadataService = Objects.requireNonNull(metadataService, "metadataService must not be null");
        this.databaseManagementService = Objects.requireNonNull(databaseManagementService, "databaseManagementService must not be null");
        this.hbaseManagementService = Objects.requireNonNull(hbaseManagementService, "hbaseManagementService must not be null");
        this.namespaceGenerationService = Objects.requireNonNull(namespaceGenerationService, "namespaceGenerationService must not be null");
        this.pinpointDataService = Objects.requireNonNull(pinpointDataService, "pinpointDataService must not be null");
    }

    @Override
    public RepositoryInfoBasic getBasicRepositoryInfo(String organizationName) {
        RepositoryInfo repositoryInfo = metadataService.getRepositoryInfo(organizationName);
        if (repositoryInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        return RepositoryInfoBasic.fromRepositoryInfo(repositoryInfo);
    }

    @Override
    public RepositoryInfoDetail getDetailedRepositoryInfo(String organizationName) {
        RepositoryInfo repositoryInfo = metadataService.getRepositoryInfo(organizationName);
        if (repositoryInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        return RepositoryInfoDetail.fromRepositoryInfo(repositoryInfo);
    }

    @Override
    public List<RepositoryInfoDetail> getDetailedRepositoryInfos() {
        return metadataService.getAllRepositoryInfo().stream()
                .map(RepositoryInfoDetail::fromRepositoryInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void createRepository(String organizationName) {
        if (StringUtils.isEmpty(organizationName)) {
            throw new IllegalArgumentException("organizationName must not be empty");
        }
        logger.info("Creating repository for organization : {}", organizationName);
        String databaseName = namespaceGenerationService.generateDatabaseName(organizationName);
        String hbaseNamespace = namespaceGenerationService.generateHbaseNamespace(organizationName);

        createOrganization(organizationName, databaseName, hbaseNamespace);

        databaseManagementService.createDatabase(databaseName);
        hbaseManagementService.createRepository(hbaseNamespace);
    }

    @Override
    public void createRepository(String organizationName, UserInfo userInfo, String password) {
        if (StringUtils.isEmpty(organizationName)) {
            throw new IllegalArgumentException("organizationName must not be empty");
        }
        if (userInfo == null) {
            throw new IllegalArgumentException("userInfo must not be null");
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password must not be empty");
        }
        logger.info("Creating repository for organization : {}", organizationName);
        String databaseName = namespaceGenerationService.generateDatabaseName(organizationName);
        String hbaseNamespace = namespaceGenerationService.generateHbaseNamespace(organizationName);

        createOrganization(organizationName, databaseName, hbaseNamespace);

        databaseManagementService.createDatabaseWithUser(organizationName, userInfo, password);
        hbaseManagementService.createRepository(hbaseNamespace);
    }

    private void createOrganization(String organizationName, String databaseName, String hbaseNamespace) {
        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace);
        try {
            metadataService.createOrganization(paaSOrganizationInfo);
        } catch (Exception e) {
            logger.error("Failed to create repository for organization : {}", organizationName, e);
            if (e instanceof RepositoryException) {
                throw e;
            }
            throw new RepositoryException(organizationName, "Failed to create repository", e);
        }
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public void updateRepository(String organizationName, boolean enable, long expireTime) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        PaaSOrganizationInfo organizationInfoForUpdate = new PaaSOrganizationInfo(organizationInfo.getOrganization(), organizationInfo.getDatabaseName(), organizationInfo.getHbaseNamespace(), enable, expireTime);
        metadataService.updateOrganizationInfo(organizationInfoForUpdate);
    }

    @Override
    public void deleteRepository(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        String databaseName = organizationInfo.getDatabaseName();
        String hbaseNamespace = organizationInfo.getHbaseNamespace();
        boolean databaseExists = databaseManagementService.databaseExists(databaseName);
        boolean hbaseExists = hbaseManagementService.namespaceExists(hbaseNamespace);
        if (databaseExists || hbaseExists) {
            throw new InvalidRepositoryStateException(organizationName, "database and hbase must not exist to delete repository");
        }
        metadataService.deleteOrganization(organizationInfo);
    }

    @Override
    public DatabaseInfo getDatabaseInfo(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String databaseName = organizationInfo.getDatabaseName();
        return databaseManagementService.getDatabaseInfo(databaseName);
    }

    @Override
    public void createDatabase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String databaseName = organizationInfo.getDatabaseName();
        try {
            if (!databaseManagementService.createDatabase(databaseName)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for creating database");
            }
        } catch (DatabaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public void updateDatabase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String databaseName = organizationInfo.getDatabaseName();
        try {
            if (!databaseManagementService.updateDatabase(databaseName)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for updating database");
            }
        } catch (DatabaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public void deleteDatabase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String databaseName = organizationInfo.getDatabaseName();
        try {
            if (!databaseManagementService.dropDatabase(databaseName)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for deleting database");
            }
        } catch (DatabaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public HbaseInfo getHbaseInfo(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        return hbaseManagementService.getHbaseInfo(hbaseNamespace);
    }

    @Override
    public void createHbase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        try {
            if (!hbaseManagementService.createRepository(hbaseNamespace)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for creating hbase");
            }
        } catch (HbaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public void updateHbase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        try {
            if (!hbaseManagementService.updateRepository(hbaseNamespace)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for updating hbase");
            }
        } catch (HbaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public void deleteHbase(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        try {
            if (!hbaseManagementService.deleteRepository(hbaseNamespace)) {
                throw new InvalidRepositoryStateException(organizationName, "Invalid state for deleting hbase");
            }
        } catch (HbaseManagementException e) {
            throw new InvalidRepositoryStateException(organizationName, e.getMessage(), e);
        }
    }

    @Override
    public List<String> getApplicationNames(String organizationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        return pinpointDataService.getApplicationNames(hbaseNamespace);
    }

    @Override
    public List<String> getAgentIds(String organizationName, String applicationName) {
        PaaSOrganizationInfo organizationInfo = metadataService.getOrganizationInfo(organizationName);
        if (organizationInfo == null) {
            throw new UnknownRepositoryException(organizationName);
        }
        final String hbaseNamespace = organizationInfo.getHbaseNamespace();
        return pinpointDataService.getAgentIds(hbaseNamespace, applicationName);
    }
}
