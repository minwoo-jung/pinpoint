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
package com.navercorp.pinpoint.web.namespace.vo;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public class PaaSCompanyInfo {

    private final String userId;
    private final String company;
    private final String databaseName;
    private final String hbaseNameSpace;

    public PaaSCompanyInfo(String company, String userId, String databaseName, String hbaseNameSpace) {
        if (StringUtils.isEmpty(company)) {
            throw new IllegalArgumentException("company must not be empty");
        }
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("databaseName must not be empty");
        }
        if (StringUtils.isEmpty(hbaseNameSpace)) {
            throw new IllegalArgumentException("hbaseNameSpace must not be empty");
        }

        this.company = company;
        this.userId = userId;
        this.databaseName = databaseName;
        this.hbaseNameSpace = hbaseNameSpace;
    }

    public String getUserId() {
        return userId;
    }

    public String getHbaseNameSpace() {
        return hbaseNameSpace;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
