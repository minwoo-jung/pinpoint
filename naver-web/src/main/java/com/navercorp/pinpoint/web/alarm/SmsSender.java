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
package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.batch.NaverBatchConfiguration;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class SmsSender {

    private static final String QUOTATATION = "\"";
    private static final String SENDER_NUMBER = "0317844499";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String smsServerUrl;
    private String smsServiceID;
    private UserGroupService userGroupService;

    public SmsSender(NaverBatchConfiguration batchConfiguration, UserGroupService userGroupService) {
        Assert.notNull(batchConfiguration, "batchConfiguration must not be null");
        Assert.notNull(userGroupService, "userGroupService must not be null");

        this.smsServerUrl = batchConfiguration.getMexServerUrl();
        this.smsServiceID = batchConfiguration.getServiceID();
        this.userGroupService = userGroupService;
    }

    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.isEmpty()) {
            return;
        }

        CloseableHttpClient client = HttpClients.createDefault();

        try {
            List<String> smsMessageList = checker.getSmsMessage();
            for(String message : smsMessageList) {
                logger.info("send SMS : {}", message);
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("serviceId", smsServiceID));
                nvps.add(new BasicNameValuePair("sendMdn", QUOTATATION + SENDER_NUMBER + QUOTATATION));
                nvps.add(new BasicNameValuePair("receiveMdnList",convertToReceiverFormat(receivers)));
                nvps.add(new BasicNameValuePair("content", QUOTATATION + message + " #" + sequenceCount + QUOTATATION));

                HttpGet get = new HttpGet(smsServerUrl + "?" + URLEncodedUtils.format(nvps, StandardCharsets.UTF_8));
                logger.debug("SMSServer url : {}", get.getURI());
                HttpResponse response = client.execute(get);
                logger.debug("SMSServer call result ={}", EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Error while HttpClient closed", e);
            }
        }
    }

    private String convertToReceiverFormat(List<String> receivers) {
        List<String> result = new ArrayList<>();

        for (String receiver : receivers) {
            result.add(QUOTATATION + receiver + QUOTATATION);
        }

        return result.toString();
    }
}
