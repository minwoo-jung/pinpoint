/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.batch;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.exception.PinpointWebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.dao.UserDao;

import java.util.List;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
public class InitListener implements StepExecutionListener {
    private static final String BATCH_NAME = "inner_system_batch";
    private static final String NAVER_ORGANIZATION_NAME = "KR";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    UserService userService;
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        insertNamespace(stepExecution);
        initTable();
    }

    private void insertNamespace(StepExecution stepExecution) {
        PaaSOrganizationInfo paaSOrganizationInfo = null;
        try {
            paaSOrganizationInfo = metaDataService.selectPaaSOrganizationInfo(BATCH_NAME, NAVER_ORGANIZATION_NAME);
        } catch (PinpointWebSocketException e) {
            logger.error("exception occured while create PaaSOrganizationInfo.",e);
            throw new RuntimeException(e);
        }

        stepExecution.getExecutionContext().put(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, paaSOrganizationInfo);
    }

    private void initTable() {
        userService.dropAndCreateUserTable();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}
