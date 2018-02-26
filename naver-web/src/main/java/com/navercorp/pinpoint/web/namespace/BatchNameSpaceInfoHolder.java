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

import org.springframework.batch.core.StepExecution;
import org.springframework.util.Assert;

/**
 * @author minwoo.jung
 */
public class BatchNameSpaceInfoHolder implements NameSpaceInfoHolder {

    private static final String BATCH_USER = "batchJob";
    private final NameSpaceInfo batchNameSpaceInfo;

    public BatchNameSpaceInfoHolder(StepExecution stepExecution) {
        Assert.notNull(stepExecution, "stepExecution must not be null.");
        String databaseName = (String) stepExecution.getExecutionContext().get(DATABASE_NAME_KEY);
        String hbaseNameSpace = (String) stepExecution.getExecutionContext().get(HBASE_NAMESAPCE);
        batchNameSpaceInfo = new NameSpaceInfo(BATCH_USER, databaseName, hbaseNameSpace);
    }

    @Override
    public NameSpaceInfo getNameSpaceInfo() throws Exception {
        return batchNameSpaceInfo;
    }
}
