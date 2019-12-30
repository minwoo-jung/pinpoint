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

import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationKey;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
@Ignore
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:applicationContext-manager.xml")
//@WebAppConfiguration
//@Transactional("metaDataTransactionManager")
public class MysqlMetadataDaoTest {

    @Autowired
    MetadataDao metadataDao;

//    @Test
    public void organizationInfoTest() {
        String organizationName = "testtest";
        String databaseName = "databaseName";
        String hbaseNamespace = "hbaseNamespace";
        boolean isEnabled = true;
        boolean isDeleted = false;
        PaaSOrganizationInfo organizationInfo = new PaaSOrganizationInfo(organizationName, databaseName, hbaseNamespace, isEnabled, isDeleted);
        metadataDao.insertPaaSOrganizationInfo(organizationInfo);
        PaaSOrganizationInfo paaSOrganizationInfo = metadataDao.selectPaaSOrganizationInfo(organizationName);

        assertEquals(organizationName, paaSOrganizationInfo.getOrganization());
        assertEquals(databaseName, paaSOrganizationInfo.getDatabaseName());
        assertEquals(hbaseNamespace, paaSOrganizationInfo.getHbaseNamespace());
        assertEquals(isEnabled, paaSOrganizationInfo.isEnabled());
        assertEquals(isDeleted, paaSOrganizationInfo.isDeleted());

        metadataDao.deletePaaSOrganizationInfo(organizationName);
    }

//    @Test
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