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

import com.navercorp.pinpoint.manager.core.RepositoryStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;

/**
 * @author HyunGil Jeong
 */
public class RepositoryInfoBasic {

    private final String organizationName;
    private final RepositoryStatus repositoryStatus;
    private final boolean isEnabled;

    private RepositoryInfoBasic(String organizationName, RepositoryStatus repositoryStatus, boolean isEnabled) {
        this.organizationName = organizationName;
        this.repositoryStatus = repositoryStatus;
        this.isEnabled = isEnabled;
    }

    public static RepositoryInfoBasic fromRepositoryInfo(RepositoryInfo repositoryInfo) {
        if (repositoryInfo == null) {
            return null;
        }
        String organizationName = repositoryInfo.getOrganizationName();
        StorageStatus databaseStatus = repositoryInfo.getDatabaseStatus();
        StorageStatus hbaseStatus = repositoryInfo.getHbaseStatus();
        RepositoryStatus repositoryStatus = RepositoryStatus.fromStorageStatuses(databaseStatus, hbaseStatus);
        boolean isEnabled = repositoryInfo.getEnable();
        return new RepositoryInfoBasic(organizationName, repositoryStatus, isEnabled);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public RepositoryStatus getRepositoryStatus() {
        return repositoryStatus;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
