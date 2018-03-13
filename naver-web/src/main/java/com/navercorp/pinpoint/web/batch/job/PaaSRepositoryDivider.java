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

import com.navercorp.pinpoint.web.batch.Divider;
import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.service.MetaDataService;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class PaaSRepositoryDivider implements Divider {

    @Autowired
    private MetaDataService metaDataService;

    @Override
    public Map<String, ExecutionContext> divide(final String partitionNamePrefix, final String batchName) {
        final Map<String, ExecutionContext> mapContext = new HashMap<>();
        final List<PaaSOrganizationInfo> paaSOrganizationInfoList = metaDataService.selectPaaSOrganizationInfoList();

        int i = 0;
        for (PaaSOrganizationInfo paaSOrganizationInfo : paaSOrganizationInfoList) {
            ExecutionContext executionContext = new ExecutionContext();
            paaSOrganizationInfo.setUserId(batchName);
            executionContext.put(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
            mapContext.put(partitionNamePrefix + i++, executionContext);
        }

        return mapContext;
    }
}
