/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
@Configuration
public class NaverBatchConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("#{naverBatchProps['alarm.sms.url']}")
    private String mexServerUrl;

    @Value("#{naverBatchProps['alarm.sms.api.key']}")
    private String apiKey;

    @Value("#{naverBatchProps['alarm.sms.sender.number']}")
    private String senderNumber;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((batchProps['alarm.sms.cellphone.number'] ?: ''), ',')}")
    private List<String> cellPhoneNumberList;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((batchProps['admin.user.list'] ?: ''), ',')}")
    private List<String> adminUserList = Collections.emptyList();

    public NaverBatchConfiguration() {
    }


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

    public List<String> getCellPhoneNumberList() {
        return cellPhoneNumberList;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NaverBatchConfiguration{");
        sb.append("mexServerUrl='").append(mexServerUrl).append('\'');
        sb.append(", apiKey='").append(apiKey).append('\'');
        sb.append(", senderNumber='").append(senderNumber).append('\'');
        sb.append(", cellPhoneNumberList=").append(cellPhoneNumberList);
        sb.append(", adminUserList=").append(adminUserList);
        sb.append('}');
        return sb.toString();
    }
}
