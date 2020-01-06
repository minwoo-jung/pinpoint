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

package com.navercorp.pinpoint.manager.dao.mysql;

import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.*;
import com.navercorp.pinpoint.manager.hbase.HBaseManagerAdminTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-manager.xml")
@WebAppConfiguration
@Transactional("metaDataTransactionManager")
public class MysqlMetadataDaoTest {

    @Autowired
    MetadataDao metadataDao;

    @Test
    public void organizationInfoTest() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean isEnabled = true;
        long expireTime = PaaSOrganizationInfo.MAX_EXPIRE_TIME;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, true, expireTime);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);
        PaaSOrganizationInfo paaSOrganizationInfo = metadataDao.selectPaaSOrganizationInfo(organizationName);

        assertEquals(organizationName, paaSOrganizationInfo.getOrganization());
        assertEquals(databaseName, paaSOrganizationInfo.getDatabaseName());
        assertEquals(hbaseNamespace, paaSOrganizationInfo.getHbaseNamespace());
        assertEquals(isEnabled, paaSOrganizationInfo.getEnable());
        assertEquals(expireTime, paaSOrganizationInfo.getExpireTimeLong());
        assertEquals("2100-12-31 23:59:59", paaSOrganizationInfo.getExpireTime());

        metadataDao.deletePaaSOrganizationInfo(organizationName);
    }

    @Test
    public void existOrganizationTest() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean isEnabled = true;
        long expireTime = PaaSOrganizationInfo.MAX_EXPIRE_TIME;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, true, expireTime);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);

        metadataDao.existDatabase(organizationName);
    }

    @Test
    public void updatePaaSOrganizationInfo() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean enable = true;
        long expireTime = PaaSOrganizationInfo.MAX_EXPIRE_TIME;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, true, expireTime);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);

        PaaSOrganizationInfo paaSOrganizationInfo = metadataDao.selectPaaSOrganizationInfo(organizationName);
        assertEquals(organizationName, paaSOrganizationInfo.getOrganization());
        assertEquals(databaseName, paaSOrganizationInfo.getDatabaseName());
        assertEquals(hbaseNamespace, paaSOrganizationInfo.getHbaseNamespace());
        assertEquals(enable, paaSOrganizationInfo.getEnable());
        assertEquals(expireTime, paaSOrganizationInfo.getExpireTimeLong());
        assertEquals("2100-12-31 23:59:59", paaSOrganizationInfo.getExpireTime());

        String updatedDatabaseName = "databaseName2";
        String updatedHbaseNamespace = "hbaseNamespace2";
        boolean updateEnable = false;
        long updatedExpireTime = 4102412399000L;
        PaaSOrganizationInfo updatedOrganizationInfo = new PaaSOrganizationInfo(organizationName, updatedDatabaseName, updatedHbaseNamespace, true, updatedExpireTime);
        metadataDao.updatePaaSOrganizationInfo(updatedOrganizationInfo);

        PaaSOrganizationInfo updatedOrgInfo = metadataDao.selectPaaSOrganizationInfo(organizationName);
        assertEquals(organizationName, updatedOrgInfo.getOrganization());
        assertEquals(updatedDatabaseName, updatedOrgInfo.getDatabaseName());
        assertEquals(updatedHbaseNamespace, updatedOrgInfo.getHbaseNamespace());
        assertEquals(enable, updatedOrgInfo.getEnable());
        assertEquals(updatedExpireTime, updatedOrgInfo.getExpireTimeLong());
        assertEquals("2099-12-31 23:59:59", updatedOrgInfo.getExpireTime());


    }

    @Test
    public void selectAllRepositoryInfoTest() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean enable = true;
        long expireTime = PaaSOrganizationInfo.MAX_EXPIRE_TIME;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, true, expireTime);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);

        DatabaseManagement databaseManagement = new DatabaseManagement();
        databaseManagement.setDatabaseName(databaseName);
        databaseManagement.setDatabaseStatus(StorageStatus.READY);
        metadataDao.insertDatabaseManagement(databaseManagement);

        HbaseManagement hbaseManagement = new HbaseManagement();
        hbaseManagement.setHbaseNamespace(hbaseNamespace);
        hbaseManagement.setHbaseStatus(StorageStatus.READY);
        metadataDao.insertHbaseManagement(hbaseManagement);

        List<RepositoryInfo> repositoryInfoList = metadataDao.selectAllRepositoryInfo();

        for (RepositoryInfo repositoryInfo : repositoryInfoList) {
            if (organizationName.equals(repositoryInfo.getOrganizationName())) {
                assertEquals(repositoryInfo.getOrganizationName(), organizationName);
                assertEquals(repositoryInfo.getDatabaseName(), databaseName);
                assertEquals(repositoryInfo.getDatabaseStatus(), StorageStatus.READY);
                assertEquals(repositoryInfo.getHbaseNamespace(), hbaseNamespace);
                assertEquals(repositoryInfo.getHbaseStatus(), StorageStatus.READY);
                assertEquals(repositoryInfo.getEnable(), enable);
                assertEquals(repositoryInfo.getExpireTimeLong(), expireTime);
            }
        }
    }

    @Test
    public void selectRepositoryInfoTest() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean enable = true;
        long expireTime = PaaSOrganizationInfo.MAX_EXPIRE_TIME;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, true, expireTime);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);

        DatabaseManagement databaseManagement = new DatabaseManagement();
        databaseManagement.setDatabaseName(databaseName);
        databaseManagement.setDatabaseStatus(StorageStatus.READY);
        metadataDao.insertDatabaseManagement(databaseManagement);

        HbaseManagement hbaseManagement = new HbaseManagement();
        hbaseManagement.setHbaseNamespace(hbaseNamespace);
        hbaseManagement.setHbaseStatus(StorageStatus.READY);
        metadataDao.insertHbaseManagement(hbaseManagement);

        RepositoryInfo repositoryInfo = metadataDao.selectRepositoryInfo(organizationName);
        assertEquals(repositoryInfo.getOrganizationName(), organizationName);
        assertEquals(repositoryInfo.getDatabaseName(), databaseName);
        assertEquals(repositoryInfo.getDatabaseStatus(), StorageStatus.READY);
        assertEquals(repositoryInfo.getHbaseNamespace(), hbaseNamespace);
        assertEquals(repositoryInfo.getHbaseStatus(), StorageStatus.READY);
        assertEquals(repositoryInfo.getEnable(), enable);
        assertEquals(repositoryInfo.getExpireTimeLong(), expireTime);
    }

    @Test
    public void organizationKeyTest() {
        String organizationName = "testtest";
        String uuid = UUID.nameUUIDFromBytes((organizationName).getBytes()).toString().replace("-", "");
        metadataDao.insertPaaSOrganizationKey(new PaaSOrganizationKey(uuid, organizationName));
        PaaSOrganizationKey paaSOrganizationKey = metadataDao.selectPaaSOrganizationKey(uuid);

        assertEquals(paaSOrganizationKey.getOrganization(), organizationName);
        assertEquals(paaSOrganizationKey.getUuKey(), uuid);

        metadataDao.deletePaaSOrganizationKey(organizationName);
    }

}