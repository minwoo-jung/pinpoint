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

import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoDetail;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoBasic;

import java.util.List;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public interface RepositoryService {

    RepositoryInfoBasic getBasicRepositoryInfo(String organizationName);

    RepositoryInfoDetail getDetailedRepositoryInfo(String organizationName);

    List<RepositoryInfoDetail> getDetailedRepositoryInfos();

    void createRepository(String organizationName);

    void updateRepository(String organizationName, Boolean isEnabled, Boolean isDeleted);

    void deleteRepository(String organizationName);

    DatabaseInfo getDatabaseInfo(String organizationName);

    void createDatabase(String organizationName);

    void updateDatabase(String organizationName);

    void deleteDatabase(String organizationName);

    HbaseInfo getHbaseInfo(String organizationName);

    void createHbase(String organizationName);

    void updateHbase(String organizationName);

    void deleteHbase(String organizationName);

    List<String> getApplicationNames(String organizationName);

}
