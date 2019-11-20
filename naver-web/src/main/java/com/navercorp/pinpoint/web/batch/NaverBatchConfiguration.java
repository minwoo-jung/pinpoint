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

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
@Configuration
public class NaverBatchConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${alarm.sms.url']}")
    private String mexServerUrl;

    @Value("${alarm.sms.api.key']}")
    private String apiKey;

    @Value("${alarm.sms.sender.number']}")
    private String senderNumber;

    @Value("${alarm.sms.cellphone.number}")
    private String[] cellPhoneNumberList = new String[0];

    @Value("${admin.user.list}")
    private String[] adminUserList = new String[0];

    public NaverBatchConfiguration() {
    }


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        logger.info("{}", this);
        AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    private String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result ;
    }

    public List<String> getAdminUserList() {
        return Arrays.asList(adminUserList);
    }

    public String getMexServerUrl() {
        return mexServerUrl;
    }

    public List<String> getCellPhoneNumberList() {
        return Arrays.asList(cellPhoneNumberList);
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
        sb.append(", cellPhoneNumberList=").append(Arrays.toString(cellPhoneNumberList));
        sb.append(", adminUserList=").append(Arrays.toString(adminUserList));
        sb.append('}');
        return sb.toString();
    }
}
