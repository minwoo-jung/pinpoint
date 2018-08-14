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
package com.navercorp.pinpoint.collector.namespace;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public class NameSpaceInfo {

    public final static String NAMESPACE_INFO = NameSpaceInfo.class.getName();

    public final static NameSpaceInfo DEFAULT = new NameSpaceInfo("navercorp", "pinpoint", "default");

    private final String organization;
    private final String mysqlDatabaseName;
    private final String hbaseNamespace;

    public NameSpaceInfo(String organization, String mysqlDatabaseName, String hbaseNamespace) {
        if (StringUtils.isEmpty(organization)) {
            throw new IllegalArgumentException("organization must not be empty");
        }
        if (StringUtils.isEmpty(mysqlDatabaseName)) {
            throw new IllegalArgumentException("mysqlDatabaseName must not be empty");
        }
        if (StringUtils.isEmpty(hbaseNamespace)) {
            throw new IllegalArgumentException("hbaseNamespace must not be empty");
        }

        this.organization = organization;
        this.hbaseNamespace = hbaseNamespace;
        this.mysqlDatabaseName = mysqlDatabaseName;
    }

    public String getHbaseNamespace() {
        return hbaseNamespace;
    }

    public String getMysqlDatabaseName() {
        return mysqlDatabaseName;
    }

    public String getOrganization() {
        return organization;
    }

    @Override
    public String toString() {
        return "NameSpaceInfo{" +
            "organization='" + organization + '\'' +
            ", hbaseNamespace='" + hbaseNamespace + '\'' +
            ", mysqlDatabaseName='" + mysqlDatabaseName + '\'' +
            '}';
    }
}
