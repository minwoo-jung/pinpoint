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
package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class NaverBatchConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("#{naverBatchProps['sms.url']}")
    private String mexServerUrl;

    @Value("#{naverBatchProps['sms.serviceId']}")
    private String serviceID;

    @Value("#{naverBatchProps['batch.server.env']}")
    private String batchEnv;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((batchProps['sms.cellphone.number'] ?: ''), ',')}")
    private List<String> cellPhoneNumberList;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((batchProps['admin.user.list'] ?: ''), ',')}")
    private List<String> adminUserList = Collections.emptyList();

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("NaverBatchConfiguration:{}", this.toString());
    }

    private String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result ;
    }

    public List<String> getAdminUserList() {
        return adminUserList;
    }

    public String getMexServerUrl() {
        return mexServerUrl;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public String getServiceID() {
        return serviceID;
    }

    public List<String> getCellPhoneNumberList() {
        return cellPhoneNumberList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NaverBatchConfiguration{");
        sb.append("mexServerUrl='").append(mexServerUrl).append('\'');
        sb.append(", serviceID='").append(serviceID).append('\'');
        sb.append(", cellPhoneNumberList=").append(cellPhoneNumberList);
        sb.append(", adminUserList=").append(adminUserList);
        sb.append('}');
        return sb.toString();
    }
}
