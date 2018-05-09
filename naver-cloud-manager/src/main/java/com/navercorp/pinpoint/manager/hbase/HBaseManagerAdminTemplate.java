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
import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HBaseManagerAdminTemplate extends HBaseAdminTemplate {

    private final AdminFactory adminFactory;

    public HBaseManagerAdminTemplate(AdminFactory adminFactory) {
        super(adminFactory);
        this.adminFactory = Objects.requireNonNull(adminFactory, "adminFactory must not be null");
    }

    public List<String> getTableNames(String namespace) {
        List<String> qualifiers = Collections.emptyList();
        Admin admin = adminFactory.getAdmin();
        try {
            TableName[] tableNames = admin.listTableNamesByNamespace(namespace);
            if (!ArrayUtils.isEmpty(tableNames)) {
                qualifiers = new ArrayList<>(tableNames.length);
                for (TableName tableName : tableNames) {
                    qualifiers.add(tableName.getQualifierAsString());
                }
            }
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
        return qualifiers;
    }
}
