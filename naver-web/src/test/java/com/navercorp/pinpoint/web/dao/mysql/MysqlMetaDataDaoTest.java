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

package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.MetaDataDao;
import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.security.MetaDataFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web-naver.xml")
@TestPropertySource(properties = {"pinpoint.profiles.active=local"})
@WebAppConfiguration
@Transactional("metaDataTransactionManager")
public class MysqlMetaDataDaoTest {

    @Autowired
    MetaDataDao metaDataDao;

    @Test
    public void select() {
        List<PaaSOrganizationInfo> paaSOrganizationInfoList = metaDataDao.selectPaaSOrganizationInfoList();

        assertTrue(paaSOrganizationInfoList.size() > 0);

        for (PaaSOrganizationInfo paaSOrganizationInfo : paaSOrganizationInfoList) {
            if (paaSOrganizationInfo.getOrganization().equals("navercorp")) {
                assertEquals(paaSOrganizationInfo.getDatabaseName(), "pinpoint");
                assertEquals(paaSOrganizationInfo.getHbaseNameSpace(), "default");
                return;
            } else {
                continue;
            }
        }

        fail();
    }

}