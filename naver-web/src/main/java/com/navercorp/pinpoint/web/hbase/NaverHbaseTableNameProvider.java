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

package com.navercorp.pinpoint.web.hbase;

import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.HbaseTableNameCache;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;

/**
 * @author HyunGil Jeong
 */
public class NaverHbaseTableNameProvider implements TableNameProvider {

    private static final HbaseTableNameCache CACHE = new HbaseTableNameCache();

    @Override
    public TableName getTableName(String tableName) {
        return CACHE.get(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, tableName);
    }
}
