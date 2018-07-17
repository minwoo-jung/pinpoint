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

import com.navercorp.pinpoint.manager.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.vo.PaaSOrganizationKey;

/**
 * @author minwoo.jung
 */
public interface MetadataDao {
    boolean existOrganization(String organizationName);

    boolean insertPaaSOrganizationInfo(String organizationName);

    void deletePaaSOrganizationInfo(String organizationName);

    PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName);

    boolean createDatabase(String organizationName);

    void dropDatabase(String organizationName);

    boolean insertPaaSOrganizationKey(PaaSOrganizationKey paaSOrganizationKey);

    boolean existPaaSOrganizationKey(String organizationKey);

    void deletePaaSOrganizationKey(String organizationName);

    PaaSOrganizationKey selectPaaSOrganizationkey(String key);
}
