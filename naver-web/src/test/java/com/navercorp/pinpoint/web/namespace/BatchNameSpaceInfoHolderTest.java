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
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class BatchNameSpaceInfoHolderTest {

    @Test
    public void get() {
        final String userId = "userId";
        final String databaseName = "pinpoint";
        final String hbaseNameSpace = "default";
        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("navercorp", userId, databaseName, hbaseNameSpace);
        StepExecution stepExecution = new StepExecution("test_step", new JobExecution(1L, new JobParameters(), "jobConfig"));
        stepExecution.getExecutionContext().put(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);

        BatchNameSpaceInfoHolder batchNameSpaceInfoHolder = new BatchNameSpaceInfoHolder(stepExecution);
        NameSpaceInfo nameSpaceInfo = batchNameSpaceInfoHolder.getNameSpaceInfo();

        assertEquals(nameSpaceInfo.getUserId(), userId);
        assertEquals(nameSpaceInfo.getMysqlDatabaseName(), databaseName);
        assertEquals(nameSpaceInfo.getHbaseNamespace(), hbaseNameSpace);
    }

    @Test(expected=IllegalArgumentException.class)
    public void get2() {
        StepExecution stepExecution = new StepExecution("test_step", new JobExecution(1L, new JobParameters(), "jobConfig"));
        BatchNameSpaceInfoHolder batchNameSpaceInfoHolder = new BatchNameSpaceInfoHolder(stepExecution);
        batchNameSpaceInfoHolder.getNameSpaceInfo();
    }

}