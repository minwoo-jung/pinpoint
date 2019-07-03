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
package com.navercorp.pinpoint.manager.domain.mysql.metadata;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class PaaSOrganizationInfo {

    private String organization;
    private String databaseName;
    private String hbaseNamespace;
    private Boolean isEnabled;
    private Boolean isDeleted;

    public PaaSOrganizationInfo() {
    }

    public PaaSOrganizationInfo(String company, String databaseName, String hbaseNamespace) {
        this(company, databaseName, hbaseNamespace, true, false);
    }

    public PaaSOrganizationInfo(String company, String databaseName, String hbaseNamespace, Boolean isEnabled, Boolean isDeleted) {
        if (StringUtils.isEmpty(company)) {
            throw new IllegalArgumentException("company must not be empty");
        }
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("databaseName must not be empty");
        }
        if (StringUtils.isEmpty(hbaseNamespace)) {
            throw new IllegalArgumentException("hbaseNamespace must not be empty");
        }
        this.organization = company;
        this.databaseName = databaseName;
        this.hbaseNamespace = hbaseNamespace;
        this.isEnabled = isEnabled;
        this.isDeleted = isDeleted;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getHbaseNamespace() {
        return hbaseNamespace;
    }

    public void setHbaseNamespace(String hbaseNamespace) {
        this.hbaseNamespace = hbaseNamespace;
    }

    public Boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
