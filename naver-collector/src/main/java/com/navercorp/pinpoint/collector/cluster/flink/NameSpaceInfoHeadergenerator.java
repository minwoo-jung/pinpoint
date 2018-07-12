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
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestAttributes;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.io.header.HeaderDataGenerator;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class NameSpaceInfoHeadergenerator implements HeaderDataGenerator {

    @Override
    public Map<String, String> generate() {
        RequestAttributes attributes = RequestContextHolder.currentAttributes();
        NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) attributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
        Assert.notNull(nameSpaceInfo, "NameSpaceInfo must not be null.");

        Map<String, String> header = new HashMap<>(1);
        header.put("organization", nameSpaceInfo.getOrganization());
        header.put("databaseName", nameSpaceInfo.getMysqlDatabaseName());
        header.put("hbaseNameSpace", nameSpaceInfo.getHbaseNamespace());
        return header;
    }
}
