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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-metadata-test.xml")
@TestPropertySource(properties = {"pinpoint.profiles.active=local"})
@ActiveProfiles("tokenAuthentication")
public class NamespaceServiceImplTest {

    @Autowired
    NamespaceService namespaceService;

    @Test
    public void selectPaaSOrganizationkey() {
        PaaSOrganizationKey paaSOrganizationKey = namespaceService.selectPaaSOrganizationkey("key");
        assertNull(paaSOrganizationKey);

        PaaSOrganizationKey paaSOrganizationKey2 =  namespaceService.selectPaaSOrganizationkey("navertestkey");
        assertEquals(paaSOrganizationKey2.getOrganization(), "navertest");
    }

    @Test
    public void selectPaaSOrganizationInfo() {
        PaaSOrganizationInfo paaSOrganizationInfo = namespaceService.selectPaaSOrganizationInfo("navercorp");
        assertEquals(paaSOrganizationInfo.getOrganization(), "navercorp");
        assertEquals(paaSOrganizationInfo.getDatabaseName(), "pinpoint");
        assertEquals(paaSOrganizationInfo.getHbaseNameSpace(), "default");

        PaaSOrganizationInfo paaSOrganizationInfo2 = namespaceService.selectPaaSOrganizationInfo("navercorp");
        assertEquals(paaSOrganizationInfo2.getOrganization(), "navercorp");
        assertEquals(paaSOrganizationInfo2.getDatabaseName(), "pinpoint");
        assertEquals(paaSOrganizationInfo2.getHbaseNameSpace(), "default");

        PaaSOrganizationInfo paaSOrganizationInfo3 = namespaceService.selectPaaSOrganizationInfo("testcomp");
        assertNull(paaSOrganizationInfo3);
    }

    @Test
    public void selectPaaSOrganizationLifeCycle() {
        List<PaaSOrganizationLifeCycle> paaSOrganizationLifeCycleList = namespaceService.selectPaaSOrganizationLifeCycle();

        for (PaaSOrganizationLifeCycle paaSOrganizationLifeCycle : paaSOrganizationLifeCycleList) {
            if ("navercorp".equals(paaSOrganizationLifeCycle.getOrganization())) {
                assertTrue(paaSOrganizationLifeCycle.isEnable());
                assertEquals(paaSOrganizationLifeCycle.getExpireTimeLong(), PaaSOrganizationLifeCycle.MAX_EXPIRE_TIME);
            }
        }
    }
}