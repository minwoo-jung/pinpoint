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
package com.navercorp.pinpoint.collector.vo;

import com.navercorp.pinpoint.common.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author minwoo.jung
 */
public class PaaSOrganizationLifeCycle {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static long MAX_EXPIRE_TIME = 4133948399000L;

    private String organization = "";
    private boolean enable = false;
    private long expireTime = MAX_EXPIRE_TIME;

    public PaaSOrganizationLifeCycle() {
    }

    public PaaSOrganizationLifeCycle(String organization, boolean enable, long expireTime) {
        this.organization = organization;
        this.enable = enable;
        this.expireTime = expireTime;
    }

    public long getExpireTimeLong() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date parsedDate = format.parse(expireTime);

        this.expireTime = parsedDate.getTime();
    }

    public String getExpireTime() {
        return DateUtils.longToDateStr(expireTime, DATE_TIME_FORMAT);
    }

    public void setExpireTimeLong(long expireTime) throws ParseException {
        this.expireTime = expireTime;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getOrganization() {
        return organization;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaaSOrganizationLifeCycle{");
        sb.append("organization='").append(organization).append('\'');
        sb.append(", enable=").append(enable);
        sb.append(", expireTime=").append(DateUtils.longToDateStr(expireTime, DATE_TIME_FORMAT) + "," + expireTime);
        sb.append('}');
        return sb.toString();
    }
}
