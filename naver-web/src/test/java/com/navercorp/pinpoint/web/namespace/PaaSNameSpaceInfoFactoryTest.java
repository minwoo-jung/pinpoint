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
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
public class PaaSNameSpaceInfoFactoryTest {

    @Test
    public void getNameSpaceInfo() throws Exception {
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        when(batchConfiguration.getBatchServerIp()).thenReturn("127.0.0.1");
        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory(batchConfiguration);

        BatchNameSpaceInfoHolder batchNameSpaceInfoHolder = mock(BatchNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(batchNameSpaceInfoHolder.getNameSpaceInfo()).thenReturn(nameSpaceInfo);
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "batchNameSpaceInfoHolder", batchNameSpaceInfoHolder);

        NameSpaceInfo result = paaSNameSpaceInfoFactory.getNameSpaceInfo();
        assertEquals(result, nameSpaceInfo);
    }

    @Test(expected = RuntimeException.class)
    public void getNameSpaceInfo2() throws Exception {
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        when(batchConfiguration.getBatchServerIp()).thenReturn("127.0.0.1");
        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory(batchConfiguration);

        BatchNameSpaceInfoHolder batchNameSpaceInfoHolder = mock(BatchNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(batchNameSpaceInfoHolder.getNameSpaceInfo()).thenThrow(new IllegalArgumentException("testException"));
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "batchNameSpaceInfoHolder", batchNameSpaceInfoHolder);

        paaSNameSpaceInfoFactory.getNameSpaceInfo();
    }

    @Test
    public void getNameSpaceInfo3() throws Exception {
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        when(batchConfiguration.getBatchServerIp()).thenReturn("127.0.0.127");
        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory(batchConfiguration);

        RequestNameSpaceInfoHolder requestNameSpaceInfoHolder = mock(RequestNameSpaceInfoHolder.class);
        NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("userId", "mysqlDatabaseName", "hbaseNamespace");
        when(requestNameSpaceInfoHolder.getNameSpaceInfo()).thenReturn(nameSpaceInfo);
        ReflectionTestUtils.setField(paaSNameSpaceInfoFactory, "requestNameSpaceInfoHolder", requestNameSpaceInfoHolder);

        NameSpaceInfo result = paaSNameSpaceInfoFactory.getNameSpaceInfo();
        assertEquals(result, nameSpaceInfo);
    }

    @Test(expected = RuntimeException.class)
    public void getNameSpaceInfo4() throws Exception {
        BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
        when(batchConfiguration.getBatchServerIp()).thenReturn("127.0.0.127");
        PaaSNameSpaceInfoFactory paaSNameSpaceInfoFactory = new PaaSNameSpaceInfoFactory(batchConfiguration);

        paaSNameSpaceInfoFactory.getNameSpaceInfo();
    }
}