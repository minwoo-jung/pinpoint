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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class NaverBatchConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);
    private Properties properties;

    private String batchEnv;
    private String mexServerUrl;
    private String serviceID;
    private List<String> cellPhoneNumberList;

    private String pinpointUrl;
    private String emailServerUrl;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        batchEnv = readString(properties, "batch.server.env", null);
        mexServerUrl = readString(properties, "alarm.sms.url", null);
        serviceID = readString(properties, "alarm.sms.serviceId", null);
        pinpointUrl = readString(properties, "pinpoint.url", null);
        emailServerUrl = readString(properties, "alarm.mail.url", null);

        String[] cellPhoneNumbers = StringUtils.split(readString(properties, "alarm.sms.cellphone.number", null), ",");
        if (cellPhoneNumbers == null) {
            this.cellPhoneNumberList = Collections.emptyList();
        } else {
            this.cellPhoneNumberList = new ArrayList<>(cellPhoneNumbers.length);
            for (String cellPhoneNumber : cellPhoneNumbers) {
                if (!StringUtils.isEmpty(cellPhoneNumber)) {
                    this.cellPhoneNumberList.add(StringUtils.trim(cellPhoneNumber));
                }
            }
        }
    }

    private String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result ;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public String getMexServerUrl() {
        return mexServerUrl;
    }

    public String getServiceID() {
        return serviceID;
    }

    public List<String> getCellPhoneNumberList() {
        return cellPhoneNumberList;
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getEmailServerUrl() {
        return emailServerUrl;
    }
}
