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
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class PaaSOrganizationInfo {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static long MAX_EXPIRE_TIME = 4133948399000L;
    private String organization;
    private String databaseName;
    private String hbaseNamespace;
    private boolean enable;
    private long expireTime;

    public PaaSOrganizationInfo() {
    }

    public PaaSOrganizationInfo(String company, String databaseName, String hbaseNamespace) {
        this(company, databaseName, hbaseNamespace, true, MAX_EXPIRE_TIME);
    }

    public PaaSOrganizationInfo(String company, String databaseName, String hbaseNamespace, Boolean isEnabled, long expireTime) {
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
        this.enable = isEnabled;
        this.expireTime = expireTime;
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

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
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
