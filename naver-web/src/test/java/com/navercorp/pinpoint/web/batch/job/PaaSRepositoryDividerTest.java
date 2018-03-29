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

package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.service.MetaDataService;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 * @author minwoo.jung
 */
public class PaaSRepositoryDividerTest {

    @Test
    public void dividerTest() {
        String partitionNamePrefix = "test_job_partition_number_";
        String batchName = "test_batch";

        List<PaaSOrganizationInfo> paaSOrganizationInfoList = new ArrayList<PaaSOrganizationInfo>();
        PaaSOrganizationInfo naver = new PaaSOrganizationInfo("NAVER", "naver_user", "naver_pinpoint", "naver_default");
        PaaSOrganizationInfo samsung = new PaaSOrganizationInfo("SAMSUNG", "samsung_user", "samsung_pinpoint", "samsung_default");
        paaSOrganizationInfoList.add(naver);
        paaSOrganizationInfoList.add(samsung);
        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.selectPaaSOrganizationInfoListForBatchPartitioning(batchName)).thenReturn(paaSOrganizationInfoList);

        PaaSRepositoryDivider divider = new PaaSRepositoryDivider();
        ReflectionTestUtils.setField(divider, "metaDataService", metaDataService);

        Map<String, ExecutionContext> executionContextMap = divider.divide(partitionNamePrefix, batchName);

        assertEquals(executionContextMap.size(), 2);

        Collection<ExecutionContext> values = executionContextMap.values();
        for (ExecutionContext executionContext : values) {
            PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) executionContext.get(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
            String organization = paaSOrganizationInfo.getOrganization();
            if (organization.equals("NAVER") || organization.equals("SAMSUNG")) {
                continue;
            } else {
                fail();
            }
        }
    }

}