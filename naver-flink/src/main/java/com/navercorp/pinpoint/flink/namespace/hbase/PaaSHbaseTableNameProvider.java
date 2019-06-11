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

package com.navercorp.pinpoint.flink.namespace.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.HbaseTableNameCache;
import com.navercorp.pinpoint.flink.namespace.FlinkAttributes;
import com.navercorp.pinpoint.flink.namespace.FlinkContextHolder;
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import org.apache.hadoop.hbase.TableName;
import org.springframework.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class PaaSHbaseTableNameProvider implements TableNameProvider {

    private static final HbaseTableNameCache CACHE = new HbaseTableNameCache();

    @Override
    public TableName getTableName(HbaseTable hBaseTable) {
        return getTableName(hBaseTable.getName());
    }

    @Override
    public TableName getTableName(String tableName) {
        FlinkAttributes flinkAttributes = FlinkContextHolder.currentAttributes();
        Object paaSOrganizationInfo = flinkAttributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        if (!(paaSOrganizationInfo instanceof PaaSOrganizationInfo)) {
            throw new IllegalStateException("Unexpected PaaSOrganizationInfo : " + paaSOrganizationInfo);
        }
        String namespace = ((PaaSOrganizationInfo) paaSOrganizationInfo).getHbaseNameSpace();
        if (StringUtils.isEmpty(namespace)) {
            throw new IllegalStateException("Hbase namespace should have been set");
        }
        return CACHE.get(namespace, tableName);
    }

    @Override
    public boolean hasDefaultNameSpace() {
        return false;
    }

}
