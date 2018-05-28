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
import org.apache.hadoop.hbase.TableName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class HBaseManagerAdminTemplate extends HBaseAdminTemplate {

    public HBaseManagerAdminTemplate(AdminFactory adminFactory) {
        super(adminFactory);
    }

    public List<String> getTableNames(String namespace) {
        return execute(admin -> {
            TableName[] tableNames = admin.listTableNamesByNamespace(namespace);
            if (!ArrayUtils.isEmpty(tableNames)) {
                return Arrays.stream(tableNames)
                        .map(TableName::getQualifierAsString)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }
}
