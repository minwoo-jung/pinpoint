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
import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HBaseManagerAdminTemplate extends HBaseAdminTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HBaseManagerAdminTemplate(AdminFactory adminFactory) {
        super(adminFactory);
    }

    public NamespaceDescriptor getNamespaceDescriptor(String namespace) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        return execute(admin -> admin.getNamespaceDescriptor(namespace));
    }

    public List<TableName> getTableNames(String namespace) {
        return execute(admin -> {
            TableName[] tableNames = admin.listTableNamesByNamespace(namespace);
            if (ArrayUtils.isEmpty(tableNames)) {
                return Collections.emptyList();
            }
            return Arrays.asList(tableNames);
        });
    }

    public HTableDescriptor getTableDescriptor(TableName tableName) {
        Objects.requireNonNull(tableName, "tableName must not be null");
        return execute(admin -> admin.getTableDescriptor(tableName));
    }

    public void dropNamespace(String namespace) {
        execute(admin -> {
            admin.deleteNamespace(namespace);
            logger.info("{} namespace deleted.", namespace);
            return null;
        });
    }
}
