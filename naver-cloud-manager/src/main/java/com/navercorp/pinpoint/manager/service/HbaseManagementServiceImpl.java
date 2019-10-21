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

import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaStatus;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import com.navercorp.pinpoint.manager.core.SchemaStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.dao.HbaseNamespaceDao;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.HbaseManagement;
import com.navercorp.pinpoint.manager.exception.hbase.DuplicateHbaseException;
import com.navercorp.pinpoint.manager.exception.hbase.HbaseManagementException;
import com.navercorp.pinpoint.manager.exception.hbase.UnknownHbaseException;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Service
public class HbaseManagementServiceImpl implements HbaseManagementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String compression;

    private final MetadataDao metadataDao;

    private final HbaseSchemaService hbaseSchemaService;

    private final HbaseNamespaceDao hbaseNamespaceDao;

    private final List<ChangeSet> changeSets;

    private final ThreadPoolTaskExecutor storageManagementTaskExecutor;

    @Autowired
    public HbaseManagementServiceImpl(
            @Value("#{pinpointManagerProps['schema.hbase.compression']}") String compression,
            MetadataDao metadataDao,
            HbaseSchemaService hbaseSchemaService,
            HbaseSchemaReader hbaseSchemaReader,
            HbaseNamespaceDao hbaseNamespaceDao,
            @Qualifier("storageManagementTaskExecutor") ThreadPoolTaskExecutor storageManagementTaskExecutor) {
        this.compression = Objects.requireNonNull(compression, "compression");
        this.metadataDao = Objects.requireNonNull(metadataDao, "metadataDao");
        this.hbaseSchemaService = Objects.requireNonNull(hbaseSchemaService, "hbaseSchemaService");
        this.hbaseNamespaceDao = Objects.requireNonNull(hbaseNamespaceDao, "hbaseNamespaceDao");
        Objects.requireNonNull(hbaseSchemaReader, "hbaseSchemaReader");
        this.changeSets = Collections.unmodifiableList(hbaseSchemaReader.loadChangeSets());
        this.storageManagementTaskExecutor = Objects.requireNonNull(storageManagementTaskExecutor, "storageManagementTaskExecutor");
    }

    @Override
    public boolean namespaceExists(String namespace) {
        return hbaseNamespaceDao.namespaceExists(namespace);
    }

    @Override
    public SchemaStatus getSchemaStatus(String namespace) {
        if (!namespaceExists(namespace)) {
            return SchemaStatus.NONE;
        }
        try {
            HbaseSchemaStatus hbaseSchemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
            switch (hbaseSchemaStatus) {
                case NONE:
                    return SchemaStatus.NONE;
//                case UNKNOWN:
//                    return SchemaStatus.UNKNOWN;
                case VALID_OUT_OF_DATE:
                    return SchemaStatus.VALID_OUT_OF_DATE;
                case VALID:
                    return SchemaStatus.VALID;
                case INVALID:
                    return SchemaStatus.INVALID;
                default:
                    return SchemaStatus.ERROR;
            }
        } catch (Exception e) {
            logger.error("Error checking schema status, namespace : {}", namespace, e);
            return SchemaStatus.ERROR;
        }
    }

    @Override
    public HbaseInfo getHbaseInfo(String namespace) {
        HbaseManagement hbaseManagement = metadataDao.selectHbaseManagement(namespace);
        if (hbaseManagement == null) {
            throw new UnknownHbaseException(namespace);
        }
        SchemaStatus schemaStatus = getSchemaStatus(namespace);
        HbaseInfo hbaseInfo = new HbaseInfo();
        hbaseInfo.setHbaseNamespace(hbaseManagement.getHbaseNamespace());
        hbaseInfo.setHbaseStatus(hbaseManagement.getHbaseStatus());
        hbaseInfo.setHbaseSchemaStatus(schemaStatus);
        return hbaseInfo;
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public boolean checkAndUpdateHbaseStatus(String namespace, StorageStatus newStatus, Set<StorageStatus> validStatuses) {
        HbaseManagement hbaseManagement = metadataDao.selectHbaseManagementForUpdate(namespace);
        if (hbaseManagement == null) {
            throw new HbaseManagementException(namespace, "Hbase management data does not exist");
        }
        StorageStatus hbaseStatus = hbaseManagement.getHbaseStatus();
        if (!validStatuses.contains(hbaseStatus)) {
            logger.warn("Skipping hbase status update for namespace : {}, expected one of : {}, but was : {}", namespace, validStatuses, hbaseStatus);
            return false;
        }
        if (!metadataDao.updateHbaseStatus(namespace, newStatus)) {
            throw new HbaseManagementException(namespace, "Failed to update hbase status");
        }
        return true;
    }

    @Override
    public boolean createRepository(String namespace) {
        if (namespaceExists(namespace)) {
            throw new DuplicateHbaseException(namespace);
        }
        if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.CREATING, EnumSet.of(StorageStatus.NONE))) {
            logger.warn("Invalid hbase status for create, namespace : {}", namespace);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting hbase namespace create task, taskId : {}, namespace : {}", taskId, namespace);
        try {
            storageManagementTaskExecutor.execute(new HbaseCreateWorkerTask(taskId, namespace));
        } catch (RejectedExecutionException e) {
            logger.error("Hbase namespace create task rejected, taskId : {}, namespace : {}", taskId, namespace, e);
            if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.NONE, EnumSet.of(StorageStatus.CREATING))) {
                logger.error("Invalid hbase status for create task rejection, namespace : {}", namespace);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean updateRepository(String namespace) {
        if (!namespaceExists(namespace)) {
            throw new UnknownHbaseException(namespace);
        }
        if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.UPDATING, EnumSet.of(StorageStatus.READY))) {
            logger.warn("Invalid hbase status for update, namespace : {}", namespace);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting hbase schema update task, taskId : {}, namespace : {}", taskId, namespace);
        try {
            storageManagementTaskExecutor.execute(new HbaseUpdateWorkerTask(taskId, namespace));
        } catch (RejectedExecutionException e) {
            logger.error("Hbase namespace update task rejected, taskId : {}, namespace : {}", taskId, namespace, e);
            if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.READY, EnumSet.of(StorageStatus.UPDATING))) {
                logger.error("Invalid hbase status for update task rejection, namespace : {}", namespace);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteRepository(String namespace) {
        if (!namespaceExists(namespace)) {
            throw new UnknownHbaseException(namespace);
        }
        if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.DELETING, EnumSet.of(StorageStatus.READY, StorageStatus.ERROR))) {
            logger.warn("Invalid hbase status for delete, namespace : {}", namespace);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting hbase namespace delete task, taskId : {}, namespace : {}", taskId, namespace);
        try {
            storageManagementTaskExecutor.execute(new HbaseDeleteWorkerTask(taskId, namespace));
        } catch (RejectedExecutionException e) {
            logger.error("Hbase namespace delete task rejected, taskId : {}, namespace : {}", taskId, namespace, e);
            if (!checkAndUpdateHbaseStatus(namespace, StorageStatus.ERROR, EnumSet.of(StorageStatus.DELETING))) {
                logger.error("Invalid hbase status for delete task rejection, namespace : {}", namespace);
            }
            return false;
        }
        return true;
    }

    private class HbaseCreateWorkerTask implements Runnable {

        private final UUID taskId;
        private final String hbaseNamespace;

        private HbaseCreateWorkerTask(UUID taskId, String hbaseNamespace) {
            this.taskId = Objects.requireNonNull(taskId, "taskId");
            this.hbaseNamespace = Objects.requireNonNull(hbaseNamespace, "hbaseNamespace");
        }

        private boolean validateHbaseState() {
            HbaseManagement currentHbaseManagement = metadataDao.selectHbaseManagement(hbaseNamespace);
            if (currentHbaseManagement == null) {
                logger.error("[{}] Hbase management data does not exist for namespace : {}", taskId, hbaseNamespace);
                return false;
            }
            StorageStatus currentHbaseStatus = currentHbaseManagement.getHbaseStatus();
            if (currentHbaseStatus != StorageStatus.CREATING) {
                logger.error("[{}] Invalid status for namespace create, namespace : {}, current status : {}", taskId, hbaseNamespace, currentHbaseStatus);
                return false;
            }
            return true;
        }

        private boolean initNamespace() {
            try {
                boolean initialized = hbaseSchemaService.init(hbaseNamespace);
                if (!initialized) {
                    logger.error("[{}] Namespace {} already exists", taskId, hbaseNamespace);
                    return false;
                }
            } catch (Exception e) {
                logger.error("[{}] Error initializing namespace : {}", hbaseNamespace);
                return false;
            }
            return true;
        }

        private boolean createTables() {
            try {
                hbaseSchemaService.update(hbaseNamespace, compression, changeSets);
                logger.info("[{}] Tables created for namespace : {}", taskId, hbaseNamespace);
                return true;
            } catch (Exception e) {
                logger.error("[{}] Error creating tables for namespace : {}", taskId, hbaseNamespace);
                return false;
            }
        }

        @Override
        public void run() {
            logger.info("[{}] Creating namespace : {}", taskId, hbaseNamespace);
            if (!validateHbaseState()) {
                logger.error("[{}] Skipping namespace create due to invalid state, namespace : {}", taskId, hbaseNamespace);
                return;
            }
            if (!initNamespace()) {
                logger.error("[{}] Failed to initialize namespace : {}", taskId, hbaseNamespace);
                metadataDao.updateHbaseStatus(hbaseNamespace, StorageStatus.ERROR);
                return;
            }
            logger.info("[{}] Namespace initialized : {}. Creating tables", taskId, hbaseNamespace);
            boolean tablesCreated = createTables();
            if (tablesCreated) {
                logger.info("[{}] Tables created for namespace : {}", taskId, hbaseNamespace);
                metadataDao.updateHbaseStatus(hbaseNamespace, StorageStatus.READY);
            } else {
                logger.error("[{}] Failed to create namespace : {}", taskId, hbaseNamespace);
                metadataDao.updateHbaseStatus(hbaseNamespace, StorageStatus.ERROR);
            }
        }
    }

    private class HbaseUpdateWorkerTask implements Runnable {

        private final UUID taskId;
        private final String hbaseNamespace;

        private HbaseUpdateWorkerTask(UUID taskId, String hbaseNamespace) {
            this.taskId = Objects.requireNonNull(taskId, "taskId");
            this.hbaseNamespace = Objects.requireNonNull(hbaseNamespace, "hbaseNamespace");
        }

        private boolean validateHbaseState() {
            HbaseManagement currentHbaseManagement = metadataDao.selectHbaseManagement(hbaseNamespace);
            if (currentHbaseManagement == null) {
                logger.error("[{}] Hbase management data does not exist for namespace : {}", taskId, hbaseNamespace);
                return false;
            }
            StorageStatus currentHbaseStatus = currentHbaseManagement.getHbaseStatus();
            if (currentHbaseStatus != StorageStatus.UPDATING) {
                logger.error("[{}] Invalid status for schema update, namespace : {}, current status : {}", taskId, hbaseNamespace, currentHbaseStatus);
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            logger.info("[{}] Updating hbase schema : {}", taskId, hbaseNamespace);
            if (!validateHbaseState()) {
                logger.error("[{}] Skipping schema update due to invalid state, namespace : {}", taskId, hbaseNamespace);
                return;
            }
            try {
                hbaseSchemaService.update(hbaseNamespace, compression, changeSets);
                logger.info("[{}] Schema updated for namespace : {}", taskId, hbaseNamespace);
                metadataDao.updateHbaseStatus(hbaseNamespace, StorageStatus.READY);
            } catch (Exception e) {
                logger.error("[{}] Error updating schema for namespace : {}", taskId, hbaseNamespace);
                metadataDao.updateHbaseStatus(hbaseNamespace, StorageStatus.ERROR);
            }
        }
    }

    private class HbaseDeleteWorkerTask implements Runnable {

        private final UUID taskId;
        private final String hbaseNamespace;

        private HbaseDeleteWorkerTask(UUID taskId, String hbaseNamespace) {
            this.taskId = Objects.requireNonNull(taskId, "taskId");
            this.hbaseNamespace = Objects.requireNonNull(hbaseNamespace, "hbaseNamespace");
        }

        private boolean validateHbaseState() {
            HbaseManagement currentHbaseManagement = metadataDao.selectHbaseManagement(hbaseNamespace);
            if (currentHbaseManagement == null) {
                logger.error("[{}] Hbase management data does not exist for namespace : {}", taskId, hbaseNamespace);
                return false;
            }
            StorageStatus currentHbaseStatus = currentHbaseManagement.getHbaseStatus();
            if (currentHbaseStatus != StorageStatus.DELETING) {
                logger.error("[{}] Invalid status for namespace delete, namespace : {}, current status : {}", taskId, hbaseNamespace, currentHbaseStatus);
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            logger.info("[{}] Deleting namespace : {}", taskId, hbaseNamespace);
            if (!validateHbaseState()) {
                logger.error("[{}] Skipping namespace delete due to invalid state, namespace : {}", taskId, hbaseNamespace);
                return;
            }
            StorageStatus result;
            try {
                hbaseNamespaceDao.deleteNamespace(hbaseNamespace);
                logger.info("[{}] Namespace deleted : {}", taskId, hbaseNamespace);
                result = StorageStatus.NONE;
            } catch (Exception e) {
                logger.error("[{}] Error deleting namespace : {}", taskId, hbaseNamespace, e);
                result = StorageStatus.ERROR;
            }
            metadataDao.updateHbaseStatus(hbaseNamespace, result);
            logger.info("[{}] Namespace delete complete for namespace : {}, status : {}", taskId, hbaseNamespace, result);
        }
    }
}
