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

import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.manager.core.StorageStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author HyunGil Jeong
 */
public class RepositoryInfo {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String organizationName;
    private String databaseName;
    private StorageStatus databaseStatus;
    private String hbaseNamespace;
    private StorageStatus hbaseStatus;
    private boolean enable;
    private long expireTime;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public StorageStatus getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(StorageStatus databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public String getHbaseNamespace() {
        return hbaseNamespace;
    }

    public void setHbaseNamespace(String hbaseNamespace) {
        this.hbaseNamespace = hbaseNamespace;
    }

    public StorageStatus getHbaseStatus() {
        return hbaseStatus;
    }

    public void setHbaseStatus(StorageStatus hbaseStatus) {
        this.hbaseStatus = hbaseStatus;
    }

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getExpireTime() {
        return DateUtils.longToDateStr(expireTime, DATE_TIME_FORMAT);
    }

    public void setExpireTime(String expireTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date parsedDate = format.parse(expireTime);

        this.expireTime = parsedDate.getTime();
    }

    public long getExpireTimeLong() {
        return expireTime;
    }
}
