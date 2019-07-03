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
package com.navercorp.pinpoint.manager.dao;

import com.navercorp.pinpoint.manager.domain.mysql.metadata.DatabaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.HbaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationKey;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;

import java.util.List;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public interface MetadataDao {

    boolean existDatabase(String databaseName);

    boolean createDatabase(String databaseName);

    boolean dropDatabase(String databaseName);

    boolean existOrganization(String organizationName);

    boolean insertPaaSOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo);

    boolean deletePaaSOrganizationInfo(String organizationName);

    PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName);

    boolean updatePaaSOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo);

    List<RepositoryInfo> selectAllRepositoryInfo();

    RepositoryInfo selectRepositoryInfo(String organizationName);

    boolean existPaaSOrganizationKey(String organizationKey);

    boolean insertPaaSOrganizationKey(PaaSOrganizationKey paaSOrganizationKey);

    boolean deletePaaSOrganizationKey(String organizationName);

    PaaSOrganizationKey selectPaaSOrganizationKey(String key);


    DatabaseManagement selectDatabaseManagement(String databaseName);

    DatabaseManagement selectDatabaseManagementForUpdate(String databaseName);

    boolean insertDatabaseManagement(DatabaseManagement databaseManagement);

    boolean updateDatabaseStatus(String databaseName, StorageStatus databaseStatus);

    boolean deleteDatabaseManagement(String databaseName);


    HbaseManagement selectHbaseManagement(String hbaseNamespace);

    HbaseManagement selectHbaseManagementForUpdate(String hbaseNamespace);

    boolean insertHbaseManagement(HbaseManagement hbaseManagement);

    boolean updateHbaseStatus(String hbaseNamespace, StorageStatus hbaseStatus);

    boolean deleteHbaseManagement(String hbaseNamespace);
}
