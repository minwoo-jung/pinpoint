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
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationKey;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;

import java.util.List;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public interface MetadataService {

    List<RepositoryInfo> getAllRepositoryInfo();

    RepositoryInfo getRepositoryInfo(String organizationName);

    boolean existOrganization(String organizationName);

    PaaSOrganizationInfo getOrganizationInfo(String organizationName);

    boolean updateOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo);

    void createOrganization(PaaSOrganizationInfo paaSOrganizationInfo);

    void deleteOrganization(PaaSOrganizationInfo paaSOrganizationInfo);

    PaaSOrganizationKey getOrganizationKey(String organizationName);

    void initDatabaseStatus(String databaseName);

    boolean deleteDatabaseStatus(String databaseName);

    void initHbaseStatus(String hbaseNamespace);

    boolean deleteHbaseStatus(String hbaseNamespace);

}
