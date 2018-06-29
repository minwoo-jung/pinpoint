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

import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-collector-naver.xml")
public class MetadataCacheImplTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MetadataCache metadatacache;

    @Test
    public void selectPaaSOrganizationkeyTest() {
        String organizationId = metadatacache.selectPaaSOrganizationkey("key");
        assertEquals(organizationId, MetadataCache.NOT_EXIST_ORGANIZATION);

        String organizationId2 = metadatacache.selectPaaSOrganizationkey("key");
        assertEquals(organizationId2, MetadataCache.NOT_EXIST_ORGANIZATION);

        String organizationId3 = metadatacache.selectPaaSOrganizationkey("navertestkey");
        assertEquals(organizationId3, "navertest");

        String organizationId4 = metadatacache.selectPaaSOrganizationkey("navertestkey");
        assertEquals(organizationId4, "navertest");
    }

    @Test
    public void selectPaaSOrganizationInfoTest() {
        PaaSOrganizationInfo paaSOrganizationInfo = metadatacache.selectPaaSOrganizationInfo("navercorp");
        assertEquals(paaSOrganizationInfo.getOrganization(), "navercorp");
        assertEquals(paaSOrganizationInfo.getDatabaseName(), "pinpoint");
        assertEquals(paaSOrganizationInfo.getHbaseNameSpace(), "default");

        PaaSOrganizationInfo paaSOrganizationInfo2 = metadatacache.selectPaaSOrganizationInfo("navercorp");
        assertEquals(paaSOrganizationInfo2.getOrganization(), "navercorp");
        assertEquals(paaSOrganizationInfo2.getDatabaseName(), "pinpoint");
        assertEquals(paaSOrganizationInfo2.getHbaseNameSpace(), "default");

        PaaSOrganizationInfo paaSOrganizationInfo3 = metadatacache.selectPaaSOrganizationInfo("testcomp");
        assertNull(paaSOrganizationInfo3);

        PaaSOrganizationInfo paaSOrganizationInfo4 = metadatacache.selectPaaSOrganizationInfo("testcomp");
        assertNull(paaSOrganizationInfo4);
    }

}