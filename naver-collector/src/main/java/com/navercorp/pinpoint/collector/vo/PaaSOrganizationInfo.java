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
package com.navercorp.pinpoint.collector.vo;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public class PaaSOrganizationInfo {

    private String organization;
    private String databaseName;
    private String hbaseNameSpace;

    public PaaSOrganizationInfo() {
    }

    public PaaSOrganizationInfo(String organization, String databaseName, String hbaseNameSpace) {
        if (StringUtils.isEmpty(organization)) {
            throw new IllegalArgumentException("organization must not be empty");
        }
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("databaseName must not be empty");
        }
        if (StringUtils.isEmpty(hbaseNameSpace)) {
            throw new IllegalArgumentException("hbaseNameSpace must not be empty");
        }

        this.organization = organization;
        this.databaseName = databaseName;
        this.hbaseNameSpace = hbaseNameSpace;
    }

    public String getHbaseNameSpace() {
        return hbaseNameSpace;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setHbaseNameSpace(String hbaseNameSpace) {
        this.hbaseNameSpace = hbaseNameSpace;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return "PaaSOrganizationInfo{" + "organization='" + organization + '\'' + ", databaseName='" + databaseName + '\'' + ", hbaseNameSpace='" + hbaseNameSpace + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaaSOrganizationInfo that = (PaaSOrganizationInfo) o;

        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;
        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null) return false;
        return hbaseNameSpace != null ? hbaseNameSpace.equals(that.hbaseNameSpace) : that.hbaseNameSpace == null;
    }

    @Override
    public int hashCode() {
        int result = organization != null ? organization.hashCode() : 0;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (hbaseNameSpace != null ? hbaseNameSpace.hashCode() : 0);
        return result;
    }
}
