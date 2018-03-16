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
package com.navercorp.pinpoint.web.namespace;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import org.springframework.batch.core.StepExecution;
import org.springframework.util.Assert;

/**
 * @author minwoo.jung
 */
public class BatchNameSpaceInfoHolder implements NameSpaceInfoHolder {

    private final NameSpaceInfo batchNameSpaceInfo;

    public BatchNameSpaceInfoHolder(StepExecution stepExecution) {
        Assert.notNull(stepExecution, "stepExecution must not be null.");

        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) stepExecution.getExecutionContext().get(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        Assert.notNull(paaSOrganizationInfo, "PaaSOrganizationInfo must not be null.");

        batchNameSpaceInfo = new NameSpaceInfo(paaSOrganizationInfo.getUserId(), paaSOrganizationInfo.getDatabaseName(), paaSOrganizationInfo.getHbaseNameSpace());
    }

    @Override
    public NameSpaceInfo getNameSpaceInfo() {
        return batchNameSpaceInfo;
    }
}
