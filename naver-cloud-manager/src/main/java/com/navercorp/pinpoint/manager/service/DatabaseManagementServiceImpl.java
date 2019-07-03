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

import com.navercorp.pinpoint.manager.core.SchemaStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.dao.RepositoryDao;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.DatabaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.repository.role.RoleInformation;
import com.navercorp.pinpoint.manager.exception.database.DatabaseManagementException;
import com.navercorp.pinpoint.manager.exception.database.DuplicateDatabaseException;
import com.navercorp.pinpoint.manager.exception.database.UnknownDatabaseException;
import com.navercorp.pinpoint.manager.jdbc.RepositoryDatabaseDetails;
import com.navercorp.pinpoint.manager.jdbc.RepositoryDatabaseDetailsContextHolder;
import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import liquibase.exception.LiquibaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author HyunGil Jeong
 */
@Service
public class DatabaseManagementServiceImpl implements DatabaseManagementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MetadataDao metadataDao;

    private final RepositoryDao repositoryDao;

    private final LiquibaseService liquibaseService;

    private final ThreadPoolTaskExecutor storageManagementTaskExecutor;

    @Autowired
    public DatabaseManagementServiceImpl(MetadataDao metadataDao,
                                         RepositoryDao repositoryDao,
                                         LiquibaseService liquibaseService,
                                         @Qualifier("storageManagementTaskExecutor") ThreadPoolTaskExecutor storageManagementTaskExecutor) {
        this.metadataDao = Objects.requireNonNull(metadataDao, "metadataDao must not be null");
        this.repositoryDao = Objects.requireNonNull(repositoryDao, "repositoryDao must not be null");
        this.liquibaseService = Objects.requireNonNull(liquibaseService, "liquibaseService must not be null");
        this.storageManagementTaskExecutor = Objects.requireNonNull(storageManagementTaskExecutor, "storageManagementTaskExecutor must not be null");
    }

    @Override
    public boolean databaseExists(String databaseName) {
        return metadataDao.existDatabase(databaseName);
    }

    @Override
    public SchemaStatus getSchemaStatus(String databaseName) {
        if (!databaseExists(databaseName)) {
            return SchemaStatus.NONE;
        }
        try {
            return liquibaseService.getDatabaseSchemaStatus(databaseName);
        } catch (SQLException | LiquibaseException e) {
            logger.error("Error checking schema status, database : {}", databaseName, e);
            return SchemaStatus.ERROR;
        }
    }

    @Override
    public DatabaseInfo getDatabaseInfo(String databaseName) {
        DatabaseManagement databaseManagement = metadataDao.selectDatabaseManagement(databaseName);
        if (databaseManagement == null) {
            throw new UnknownDatabaseException(databaseName);
        }
        SchemaStatus schemaStatus = getSchemaStatus(databaseName);
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setDatabaseName(databaseManagement.getDatabaseName());
        databaseInfo.setDatabaseStatus(databaseManagement.getDatabaseStatus());
        databaseInfo.setDatabaseSchemaStatus(schemaStatus);
        return databaseInfo;
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = false)
    public boolean checkAndUpdateDatabaseStatus(String databaseName, StorageStatus newStatus, Set<StorageStatus> validStatuses) {
        DatabaseManagement databaseManagement = metadataDao.selectDatabaseManagementForUpdate(databaseName);
        if (databaseManagement == null) {
            throw new DatabaseManagementException(databaseName, "Database management data does not exist");
        }
        StorageStatus databaseStatus = databaseManagement.getDatabaseStatus();
        if (!validStatuses.contains(databaseStatus)) {
            logger.warn("Skipping database status update for : {}, expected one of : {}, but was : {}", databaseName, validStatuses, databaseStatus);
            return false;
        }
        if (!metadataDao.updateDatabaseStatus(databaseName, newStatus)) {
            throw new DatabaseManagementException(databaseName, "Failed to update database status");
        }
        return true;
    }

    @Override
    public boolean createDatabase(String databaseName) {
        if (metadataDao.existDatabase(databaseName)) {
            throw new DuplicateDatabaseException(databaseName);
        }
        if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.CREATING, EnumSet.of(StorageStatus.NONE))) {
            logger.warn("Invalid database status for create, database : {}", databaseName);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting database create task, taskId : {}, database : {}", taskId, databaseName);
        try {
            storageManagementTaskExecutor.execute(new DatabaseCreateWorkerTask(taskId, databaseName));
        } catch (RejectedExecutionException e) {
            logger.error("Database create task rejected, taskId : {}, database : {}", taskId, databaseName, e);
            if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.NONE, EnumSet.of(StorageStatus.CREATING))) {
                logger.error("Invalid database status for create task rejection, database : {}", databaseName);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean updateDatabase(String databaseName) {
        if (!metadataDao.existDatabase(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.UPDATING, EnumSet.of(StorageStatus.READY))) {
            logger.warn("Invalid database status for update, database : {}", databaseName);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting database schema update task, taskId : {}, database : {}", taskId, databaseName);
        try {
            storageManagementTaskExecutor.execute(new DatabaseUpdateWorkerTask(taskId, databaseName));
        } catch (RejectedExecutionException e) {
            logger.error("Database schema update task rejected, taskId : {}, database : {}", taskId, databaseName, e);
            if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.READY, EnumSet.of(StorageStatus.UPDATING))) {
                logger.error("Invalid database status for update task rejection, database : {}", databaseName);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean dropDatabase(String databaseName) {
        if (!metadataDao.existDatabase(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.DELETING, EnumSet.of(StorageStatus.READY, StorageStatus.ERROR))) {
            logger.warn("Invalid database status for delete, database : {}", databaseName);
            return false;
        }
        final UUID taskId = UUID.randomUUID();
        logger.info("Submitting database delete task, taskId : {}, database : {}", taskId, databaseName);
        try {
            storageManagementTaskExecutor.execute(new DatabaseDeleteWorkerTask(taskId, databaseName));
        } catch (RejectedExecutionException e) {
            logger.error("Database delete task rejected, taskId : {}, database : {}", taskId, databaseName, e);
            if (!checkAndUpdateDatabaseStatus(databaseName, StorageStatus.ERROR, EnumSet.of(StorageStatus.DELETING))) {
                logger.error("Invalid database status for delete task rejection, database : {}", databaseName);
            }
            return false;
        }
        return true;
    }

    private class DatabaseCreateWorkerTask implements Runnable {

        private final UUID taskId;
        private final String databaseName;

        private DatabaseCreateWorkerTask(UUID taskId, String databaseName) {
            this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
            this.databaseName = Objects.requireNonNull(databaseName, "databaseName must not be null");
        }

        private boolean validateDatabaseState() {
            DatabaseManagement currentDatabaseManagement = metadataDao.selectDatabaseManagement(databaseName);
            if (currentDatabaseManagement == null) {
                logger.error("[{}] Database management data does not exist for database : {}", taskId, databaseName);
                return false;
            }
            StorageStatus currentDatabaseStatus = currentDatabaseManagement.getDatabaseStatus();
            if (currentDatabaseStatus != StorageStatus.CREATING) {
                logger.error("[{}] Invalid status for database create, database : {}, current status : {}", taskId, databaseName, currentDatabaseStatus);
                return false;
            }
            return true;
        }

        private boolean createDatabase() {
            try {
                return metadataDao.createDatabase(databaseName);
            } catch (Exception e) {
                logger.error("[{}] Error creating database : {}", taskId, databaseName, e);
                return false;
            }
        }

        private boolean createTables() {
            try {
                liquibaseService.createRepository(databaseName);
                logger.info("[{}] Tables created for database : {}", taskId, databaseName);
                initData();
                logger.info("[{}] Data initialized for database : {}", taskId, databaseName);
                return true;
            } catch (Exception e) {
                logger.error("[{}] Error creating tables for database : {}", taskId, databaseName, e);
                return false;
            }
        }

        private void initData() {
            RepositoryDatabaseDetails repositoryDatabaseDetails = new RepositoryDatabaseDetails(databaseName);
            RepositoryDatabaseDetailsContextHolder.setRepositoryDatabaseDetails(repositoryDatabaseDetails);
            try {
                repositoryDao.insertRoleDefinition(RoleInformation.ADMIN_ROLE);
            } finally {
                RepositoryDatabaseDetailsContextHolder.resetRepositoryDatabaseDetails();
            }
        }

        @Override
        public void run() {
            logger.info("[{}] Creating database : {}", taskId, databaseName);
            if (!validateDatabaseState()) {
                return;
            }
            if (!createDatabase()) {
                logger.error("[{}] Failed to create database : {}", taskId, databaseName);
                metadataDao.updateDatabaseStatus(databaseName, StorageStatus.ERROR);
                return;
            }
            logger.info("[{}] Database created : {}. Creating tables", taskId, databaseName);
            boolean tablesCreated = createTables();
            if (tablesCreated) {
                logger.info("[{}] Database created for database : {}", taskId, databaseName);
                metadataDao.updateDatabaseStatus(databaseName, StorageStatus.READY);
            } else {
                logger.error("[{}] Failed to create database : {}", taskId, databaseName);
                metadataDao.updateDatabaseStatus(databaseName, StorageStatus.ERROR);
            }
        }
    }

    private class DatabaseUpdateWorkerTask implements Runnable {

        private final UUID taskId;
        private final String databaseName;

        private DatabaseUpdateWorkerTask(UUID taskId, String databaseName) {
            this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
            this.databaseName = Objects.requireNonNull(databaseName, "databaseName must not be null");
        }

        private boolean validateDatabaseState() {
            DatabaseManagement currentDatabaseManagement = metadataDao.selectDatabaseManagement(databaseName);
            if (currentDatabaseManagement == null) {
                logger.error("[{}] Database management data does not exist for database : {}", taskId, databaseName);
                return false;
            }
            StorageStatus currentDatabaseStatus = currentDatabaseManagement.getDatabaseStatus();
            if (currentDatabaseStatus != StorageStatus.UPDATING) {
                logger.error("[{}] Invalid status for schema update, database : {}, current status : {}", taskId, databaseName, currentDatabaseStatus);
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            logger.info("[{}] Updating database schema : {}", taskId, databaseName);
            if (!validateDatabaseState()) {
                logger.error("[{}] Skipping schema update due to invalid state, database : {}", taskId, databaseName);
                return;
            }
            try {
                liquibaseService.updateSchema(databaseName);
                logger.info("[{}] Schema updated for database : {}", taskId, databaseName);
                metadataDao.updateDatabaseStatus(databaseName, StorageStatus.READY);
            } catch (Exception e) {
                logger.error("[{}] Error updating schema for database : {}", taskId, databaseName, e);
                metadataDao.updateDatabaseStatus(databaseName, StorageStatus.ERROR);
            }
        }
    }

    private class DatabaseDeleteWorkerTask implements Runnable {

        private final UUID taskId;
        private final String databaseName;

        private DatabaseDeleteWorkerTask(UUID taskId, String databaseName) {
            this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
            this.databaseName = Objects.requireNonNull(databaseName, "databaseName must not be null");
        }

        private boolean validateDatabaseState() {
            DatabaseManagement currentDatabaseManagement = metadataDao.selectDatabaseManagement(databaseName);
            if (currentDatabaseManagement == null) {
                logger.error("[{}] Database management data does not exist for database : {}", taskId, databaseName);
                return false;
            }
            StorageStatus currentDatabaseStatus = currentDatabaseManagement.getDatabaseStatus();
            if (currentDatabaseStatus != StorageStatus.DELETING) {
                logger.error("[{}] Invalid status for database delete, database : {}, current status : {}", taskId, databaseName, currentDatabaseStatus);
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            logger.info("[{}] Deleting database : {}", taskId, databaseName);
            if (!validateDatabaseState()) {
                logger.error("[{}] Skipping database delete due to invalid state, database : {}", taskId, databaseName);
                return;
            }
            StorageStatus result = StorageStatus.NONE;
            try {
                metadataDao.dropDatabase(databaseName);
                if (metadataDao.existDatabase(databaseName)) {
                    result = StorageStatus.ERROR;
                }
            } catch (Exception e) {
                logger.error("[{}] Error deleting database : {}", taskId, databaseName);
                result = StorageStatus.ERROR;
            }
            metadataDao.updateDatabaseStatus(databaseName, result);
            logger.info("[{}] Database delete complete for database : {}, status : {}", taskId, databaseName, result);
        }
    }
}
