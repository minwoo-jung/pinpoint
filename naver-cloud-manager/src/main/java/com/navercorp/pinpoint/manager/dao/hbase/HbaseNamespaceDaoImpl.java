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

package com.navercorp.pinpoint.manager.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.manager.dao.HbaseNamespaceDao;
import com.navercorp.pinpoint.manager.hbase.HBaseManagerAdminTemplate;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseNamespaceDaoImpl implements HbaseNamespaceDao {

    @Autowired
    private HBaseManagerAdminTemplate adminTemplate;

    @Override
    public boolean namespaceExists(String namespace) {
        try {
            return adminTemplate.getNamespaceDescriptor(namespace) != null;
        } catch (HbaseSystemException e) {
            return false;
        }
    }

    @Override
    public List<TableName> getTableNames(String namespace) {
        return adminTemplate.getTableNames(namespace);
    }

    @Override
    public HTableDescriptor getTableDescriptor(String namespace, String tableQualifier) {
        TableName tableName = TableName.valueOf(namespace, tableQualifier);
        return adminTemplate.getTableDescriptor(tableName);
    }

    @Override
    public void deleteNamespace(String namespace) {
        List<TableName> tableNames = getTableNames(namespace);
        for (TableName tableName : tableNames) {
            adminTemplate.dropTable(tableName);
        }
        adminTemplate.dropNamespace(namespace);
    }
}
