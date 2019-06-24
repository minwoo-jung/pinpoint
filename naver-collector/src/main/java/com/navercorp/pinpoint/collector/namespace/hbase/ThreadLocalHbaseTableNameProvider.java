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

package com.navercorp.pinpoint.collector.namespace.hbase;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestAttributes;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.HbaseTableNameCache;
import org.apache.hadoop.hbase.TableName;
import org.springframework.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ThreadLocalHbaseTableNameProvider implements TableNameProvider {

    private static final HbaseTableNameCache CACHE = new HbaseTableNameCache();

    private final boolean useDefault;

    public ThreadLocalHbaseTableNameProvider() {
        this(false);
    }

    public ThreadLocalHbaseTableNameProvider(boolean useDefault) {
        this.useDefault = useDefault;
    }

    @Override
    public TableName getTableName(HbaseTable hBaseTable) {
        return getTableName(hBaseTable.getName());
    }

    @Override
    public TableName getTableName(String tableName) {
        NameSpaceInfo nameSpaceInfo = getNameSpaceInfo();
        String hbaseNamespace = nameSpaceInfo.getHbaseNamespace();
        if (StringUtils.isEmpty(hbaseNamespace)) {
            throw new IllegalStateException("hbaseNamespace must not be empty");
        }
        return CACHE.get(hbaseNamespace, tableName);
    }

    private NameSpaceInfo getNameSpaceInfo() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentAttributes();
            NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) requestAttributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
            return nameSpaceInfo;
        } catch (IllegalStateException e) {
            if (!hasDefaultNameSpace()) {
                throw e;
            }
        }
        return NameSpaceInfo.DEFAULT;
    }

    @Override
    public boolean hasDefaultNameSpace() {
        return useDefault;
    }

}
