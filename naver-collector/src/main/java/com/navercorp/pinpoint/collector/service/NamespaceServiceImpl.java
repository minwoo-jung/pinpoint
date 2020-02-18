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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.MetadataDao;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service("namespaceService")
@Transactional(rollbackFor = {Exception.class})
@Profile("tokenAuthentication")
public class NamespaceServiceImpl implements NamespaceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MetadataDao metadataDao;

    @Autowired
    public NamespaceServiceImpl(MetadataDao metadataDao) {
        this.metadataDao = Objects.requireNonNull(metadataDao, "metadataDao");
    }

    @Override
    @Transactional(readOnly = true)
    public PaaSOrganizationKey selectPaaSOrganizationkey(String licenseKey) {
        return metadataDao.selectPaaSOrganizationkey(licenseKey);
    }

    @Override
    @Transactional(readOnly = true)
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        if (organizationName == null) {
            return null;
        }
        return metadataDao.selectPaaSOrganizationInfo(organizationName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaaSOrganizationLifeCycle> selectPaaSOrganizationLifeCycle() {
        return metadataDao.selectPaaSOrganizationLifeCycle();
    }

}