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

package com.navercorp.pinpoint.manager.hbase;

import com.navercorp.pinpoint.common.hbase.AdminFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class HBaseManagerAdminTemplateTest {

    @Mock
    private Admin admin;

    @Mock
    private AdminFactory adminFactory;

    @Before
    public void init() {
        when(adminFactory.getAdmin()).thenReturn(admin);
        doNothing().when(adminFactory).releaseAdmin(admin);
    }

    @Test
    public void getTableNamesShouldReturnEmptyListForNonExistentNamespace() throws IOException {
        // Given
        when(admin.listTableNamesByNamespace(anyString())).thenReturn(new TableName[0]);
        HBaseManagerAdminTemplate adminTemplate = new HBaseManagerAdminTemplate(adminFactory);
        // When
        List<String> tableNames = adminTemplate.getTableNames("namespace");
        // Then
        Assert.assertEquals(Collections.emptyList(), tableNames);
    }
}
