/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.hbase.schema.core.ChangeSetManager;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaStatus;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaVerifier;
import com.navercorp.pinpoint.hbase.schema.core.command.HbaseSchemaCommandManager;
import com.navercorp.pinpoint.hbase.schema.core.command.TableCommand;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import com.navercorp.pinpoint.hbase.schema.service.SchemaChangeLogService;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaServiceImpl extends com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaServiceImpl {

    private final HbaseAdminClient hbaseAdminClient;

    public HbaseSchemaServiceImpl(HbaseAdminOperation hbaseAdminOperation, SchemaChangeLogService schemaChangeLogService, HbaseSchemaVerifier<HTableDescriptor> hbaseSchemaVerifier, HbaseAdminClient hbaseAdminClient) {
        super(hbaseAdminOperation, schemaChangeLogService, hbaseSchemaVerifier);
        this.hbaseAdminClient = hbaseAdminClient;
    }

    @Override
    protected boolean createNamespaceIfNotExists(String namespace) {
        return hbaseAdminClient.createNamespaceIfNotExists(namespace);
    }
}
