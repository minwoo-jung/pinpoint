/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.manager.dao.ApplicationIndexDao;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    private final HbaseOperations2 hbaseOperations2;

    @Autowired
    public HbaseApplicationIndexDao(HbaseOperations2 hbaseOperations2) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
    }

    @Override
    public List<String> getApplicationNames(String namespace) {
        HbaseTable applicationIndexTable = HbaseColumnFamily.APPLICATION_INDEX_AGENTS.getTable();
        byte[] applicationIndexAgentCf = HbaseColumnFamily.APPLICATION_INDEX_AGENTS.getName();

        TableName applicationIndexTableName = TableName.valueOf(namespace, applicationIndexTable.getName());

        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addFamily(applicationIndexAgentCf);

        RowMapper<String> applicationNameMapper = (result, rowNum) -> {
            if (result.isEmpty()) {
                return null;
            }
            // Simply get application name from row.
            // Ignore service types stored as columns for now as they are not needed.
            return Bytes.toString(result.getRow());
        };
        return hbaseOperations2.find(applicationIndexTableName, scan, applicationNameMapper).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAgentIds(String namespace, String applicationName) {
        HbaseTable applicationIndexTable = HbaseColumnFamily.APPLICATION_INDEX_AGENTS.getTable();
        byte[] applicationIndexAgentCf = HbaseColumnFamily.APPLICATION_INDEX_AGENTS.getName();

        TableName applicationIndexTableName = TableName.valueOf(namespace, applicationIndexTable.getName());

        byte[] rowKey = Bytes.toBytes(applicationName);
        Get get = new Get(rowKey);
        get.addFamily(applicationIndexAgentCf);

        RowMapper<List<String>> agentIdsMapper = (result, rowNum) -> {
            if (result.isEmpty()) {
                return Collections.emptyList();
            }
            return Stream.of(result.rawCells())
                    .map(cell -> Bytes.toString(CellUtil.cloneQualifier(cell)))
                    .collect(Collectors.toList());
        };
        return hbaseOperations2.get(applicationIndexTableName, get, agentIdsMapper);
    }
}
