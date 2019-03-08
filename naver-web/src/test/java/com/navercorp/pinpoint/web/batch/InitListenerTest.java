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

package com.navercorp.pinpoint.web.batch;

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.exception.PinpointWebSocketException;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class InitListenerTest {

    @Test
    public void beforeStepTest() throws PinpointWebSocketException {
        InitListener initListener = new InitListener();

        MetaDataService metaDataService = mock(MetaDataService.class);
        when(metaDataService.selectPaaSOrganizationInfo("inner_system_batch", "KR")).thenReturn(new PaaSOrganizationInfo("KR", "inner_system_batch", "pinpoint", "default"));
        ReflectionTestUtils.setField(initListener, "metaDataService", metaDataService);
        UserService userService = mock(UserService.class);
        ReflectionTestUtils.setField(initListener, "userService", userService);
        RoleService roleService = mock(RoleService.class);
        ReflectionTestUtils.setField(initListener, "roleService", roleService);

        StepExecution stepExecution = new StepExecution("test_step", new JobExecution(1L, new JobParameters(), "jobConfig"));

        initListener.beforeStep(stepExecution);

        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) stepExecution.getExecutionContext().get(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        assertEquals(paaSOrganizationInfo.getUserId(), "inner_system_batch");
        assertEquals(paaSOrganizationInfo.getDatabaseName(), "pinpoint");
        assertEquals(paaSOrganizationInfo.getHbaseNameSpace(), "default");
        assertEquals(paaSOrganizationInfo.getOrganization(), "KR");
    }
}