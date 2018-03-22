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

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.namespace.websocket.WebSocketAttributes;
import com.navercorp.pinpoint.web.namespace.websocket.WebSocketContextHolder;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class PaaSNameSpaceInfoFactoryTest {

    @Test
    public void batchScopeTest() throws Exception {
        StepSynchronizationManager.register(new StepExecution("test_step", new JobExecution(new Date().getTime())));

        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory();
        BatchNameSpaceInfoHolder batchNameSpaceInfoHolder = mock(BatchNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(batchNameSpaceInfoHolder.getNameSpaceInfo()).thenReturn(nameSpaceInfo);
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "batchNameSpaceInfoHolder", batchNameSpaceInfoHolder);

        NameSpaceInfo result = paaSNameSpaceInfoFactory.getNameSpaceInfo();
        assertEquals(result, nameSpaceInfo);

        StepSynchronizationManager.release();
    }

    @Test
    public void requestScopeTest() throws Exception {
        RequestContextInitializer requestContextInitializer = new RequestContextInitializer();
        requestContextInitializer.before();

        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory();
        RequestNameSpaceInfoHolder requestNameSpaceInfoHolder = mock(RequestNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(requestNameSpaceInfoHolder.getNameSpaceInfo()).thenReturn(nameSpaceInfo);
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "requestNameSpaceInfoHolder", requestNameSpaceInfoHolder);

        NameSpaceInfo result = paaSNameSpaceInfoFactory.getNameSpaceInfo();
        assertEquals(result, nameSpaceInfo);

        requestContextInitializer.after();

        assertTrue(RequestContextHolder.getRequestAttributes() == null);
    }

    @Test
    public void webSocketScopeTest() throws Exception {
        WebSocketAttributes webSocketAttributes  = new WebSocketAttributes(new ConcurrentHashMap<String, Object>());
        WebSocketContextHolder.setAttributes(webSocketAttributes);

        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory();
        WebSocketNameSpaceInfoHolder webSocketNameSpaceInfoHolder = mock(WebSocketNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(webSocketNameSpaceInfoHolder.getNameSpaceInfo()).thenReturn(nameSpaceInfo);
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "webSocketNameSpaceInfoHolder", webSocketNameSpaceInfoHolder);

        NameSpaceInfo result = paaSNameSpaceInfoFactory.getNameSpaceInfo();
        assertEquals(result, nameSpaceInfo);

        WebSocketContextHolder.resetAttributes();
        assertTrue(WebSocketContextHolder.getAttributes() == null);
    }

    @Test(expected = RuntimeException.class)
    public void nonScopeTest() throws Exception {
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        when(batchConfiguration.getBatchServerIp()).thenReturn("127.0.0.127");
        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory();

        paaSNameSpaceInfoFactory.getNameSpaceInfo();
    }
}