/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.MetaDataDao;
import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.vo.exception.PinpointWebSocketException;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(transactionManager="metaDataTransactionManager", rollbackFor = {Exception.class})
public class MetaDataServiceImpl implements MetaDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MetaDataDao metaDataDao;

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public List<PaaSOrganizationInfo> selectPaaSOrganizationInfoList() {
        return metaDataDao.selectPaaSOrganizationInfoList();
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public List<PaaSOrganizationInfo> selectPaaSOrganizationInfoListForBatchPartitioning(String batchName) {
        final List<PaaSOrganizationInfo> paaSOrganizationInfoList = selectPaaSOrganizationInfoList();
        Map<RepositoryKey, PaaSOrganizationInfo> removeDupliPaaSOrg = new HashedMap();

        for (PaaSOrganizationInfo paaSOrganizationInfo : paaSOrganizationInfoList) {
            RepositoryKey repositoryKey = new RepositoryKey(paaSOrganizationInfo.getDatabaseName(), paaSOrganizationInfo.getHbaseNameSpace());
            if (removeDupliPaaSOrg.get(repositoryKey) == null) {
                paaSOrganizationInfo.setUserId(batchName);
                removeDupliPaaSOrg.put(repositoryKey, paaSOrganizationInfo);
            } else {
                logger.info("duplicate RepositoryKey : ({}), ({})", paaSOrganizationInfo, removeDupliPaaSOrg.get(repositoryKey));
            }
        }

        return new ArrayList<>(removeDupliPaaSOrg.values());
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String userId, String organizationName) throws PinpointWebSocketException {
        PaaSOrganizationInfo paaSOrganizationInfo = metaDataDao.selectPaaSOrganizationInfo(organizationName);

        if (Objects.isNull(paaSOrganizationInfo)) {
            logger.error("can not found organizationName : userId({}), organizationName({}).", userId, organizationName);
            throw new PinpointWebSocketException(String.format("can not create PaaSOrganizationInfo : userId(%s), organizationName(%s).", userId, organizationName));
        }

        paaSOrganizationInfo.setUserId(userId);
        return paaSOrganizationInfo;
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public boolean allocatePaaSOrganizationInfoRequestScope(String userId, String organizationName) {
        return allocatePaaSOrganizationInfo(userId, organizationName, RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    @Transactional(transactionManager="metaDataTransactionManager", readOnly = true, rollbackFor = {Exception.class})
    public boolean allocatePaaSOrganizationInfoSessionScope(String userId, String organizationName) {
        return allocatePaaSOrganizationInfo(userId, organizationName, RequestAttributes.SCOPE_SESSION);
    }

    private boolean allocatePaaSOrganizationInfo(String userId, String organizationName, int requestAttributes) {
        PaaSOrganizationInfo paaSOrganizationInfo = metaDataDao.selectPaaSOrganizationInfo(organizationName);
        if (Objects.isNull(paaSOrganizationInfo)) {
            logger.error("can not found organizationName : userId({}), organizationName({}).", userId, organizationName);
            return false;
        }
        paaSOrganizationInfo.setUserId(userId);

        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        attributes.setAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo, requestAttributes);

        return true;
    }

    private static class RepositoryKey {
        private final String databaseName;
        private final String hbaseNameSpace;

        public RepositoryKey(String databaseName, String hbaseNameSpace) {
            if (StringUtils.isEmpty(databaseName)) {
                throw new IllegalArgumentException("databaseName must not be empty.");
            }
            if (StringUtils.isEmpty(hbaseNameSpace)) {
                throw new IllegalArgumentException("databaseName must not be empty.");
            }

            this.databaseName = databaseName;
            this.hbaseNameSpace = hbaseNameSpace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RepositoryKey that = (RepositoryKey) o;

            if (!databaseName.equals(that.databaseName)) return false;
            return hbaseNameSpace.equals(that.hbaseNameSpace);

        }

        @Override
        public int hashCode() {
            int result = databaseName.hashCode();
            result = 31 * result + hbaseNameSpace.hashCode();
            return result;
        }
    }
}
