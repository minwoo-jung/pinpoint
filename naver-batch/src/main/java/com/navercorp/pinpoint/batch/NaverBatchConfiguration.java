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

import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class NaverBatchConfiguration  implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("#{naverBatchProps['alarm.sms.url']}")
    private String mexServerUrl;

    @Value("#{naverBatchProps['alarm.sms.api.key']}")
    private String apiKey;

    @Value("#{naverBatchProps['alarm.sms.sender.number']}")
    private String senderNumber;

    @Value("#{naverBatchProps['batch.server.env']}")
    private String batchEnv;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((naverBatchProps['alarm.sms.cellphone.number'] ?: ''), ',')}")
    private List<String> cellPhoneNumberList;

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

    public String getBatchEnv() {
        return batchEnv;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NaverBatchConfiguration{");
        sb.append("mexServerUrl='").append(mexServerUrl).append('\'');
        sb.append(", apiKey='").append(apiKey).append('\'');
        sb.append(", senderNumber='").append(senderNumber).append('\'');
        sb.append(", batchEnv='").append(batchEnv).append('\'');
        sb.append(", cellPhoneNumberList=").append(cellPhoneNumberList);
        sb.append('}');
        return sb.toString();
    }
}