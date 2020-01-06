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
    private final boolean enable;
    private final long expireTime;

    private RepositoryInfoDetail(String organizationName,
                                 String databaseName,
                                 StorageStatus databaseStatus,
                                 String hbaseNamespace,
                                 StorageStatus hbaseStatus,
                                 boolean enable,
                                 long expireTime) {
        this.organizationName = organizationName;
        this.databaseName = databaseName;
        this.databaseStatus = databaseStatus;
        this.hbaseNamespace = hbaseNamespace;
        this.hbaseStatus = hbaseStatus;
        this.enable = enable;
        this.expireTime = expireTime;
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
        boolean enable = repositoryInfo.getEnable();
        long expireTime = repositoryInfo.getExpireTimeLong();
        return new RepositoryInfoDetail(organizationName, databaseName, databaseStatus, hbaseNamespace, hbaseStatus, enable, expireTime);
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

    public boolean isEnable() {
        return enable;
    }

    public long getExpireTime() {
        return expireTime;
    }
}
