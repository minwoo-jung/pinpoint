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

package com.navercorp.pinpoint.manager.vo.repository;

import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;

/**
 * @author HyunGil Jeong
 */
public class RepositoryInfoDetail {

    private final String organizationName;
    private final String databaseName;
    private final StorageStatus databaseStatus;
    private final String hbaseNamespace;
    private final StorageStatus hbaseStatus;
    private final boolean isEnabled;
    private final boolean isDeleted;

    private RepositoryInfoDetail(String organizationName,
                                 String databaseName,
                                 StorageStatus databaseStatus,
                                 String hbaseNamespace,
                                 StorageStatus hbaseStatus,
                                 boolean isEnabled,
                                 boolean isDeleted) {
        this.organizationName = organizationName;
        this.databaseName = databaseName;
        this.databaseStatus = databaseStatus;
        this.hbaseNamespace = hbaseNamespace;
        this.hbaseStatus = hbaseStatus;
        this.isEnabled = isEnabled;
        this.isDeleted = isDeleted;
    }

    public static RepositoryInfoDetail fromRepositoryInfo(RepositoryInfo repositoryInfo) {
        if (repositoryInfo == null) {
            return null;
        }
        String organizationName = repositoryInfo.getOrganizationName();
        String databaseName = repositoryInfo.getDatabaseName();
        StorageStatus databaseStatus = repositoryInfo.getDatabaseStatus();
        String hbaseNamespace = repositoryInfo.getHbaseNamespace();
        StorageStatus hbaseStatus = repositoryInfo.getHbaseStatus();
        boolean isEnabled = repositoryInfo.isEnabled();
        boolean isDeleted = repositoryInfo.isDeleted();
        return new RepositoryInfoDetail(organizationName, databaseName, databaseStatus, hbaseNamespace, hbaseStatus, isEnabled, isDeleted);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public StorageStatus getDatabaseStatus() {
        return databaseStatus;
    }

    public String getHbaseNamespace() {
        return hbaseNamespace;
    }

    public StorageStatus getHbaseStatus() {
        return hbaseStatus;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
