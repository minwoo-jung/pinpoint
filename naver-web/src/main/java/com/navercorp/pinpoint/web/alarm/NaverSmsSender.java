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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class NaverSmsSender implements SmsSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpHeaders headers;
    private final String smsServerUrl;
    private final UserGroupService userGroupService;
    private final RestTemplate restTemplate;
    private final String senderNumber;

    public NaverSmsSender(NaverBatchConfiguration batchConfiguration, UserGroupService userGroupService, RestTemplate restTemplate) {
        Assert.notNull(batchConfiguration, "batchConfiguration must not be null.");
        Assert.notNull(userGroupService, "userGroupService must not be null.");

        this.smsServerUrl = batchConfiguration.getMexServerUrl();
        this.senderNumber = batchConfiguration.getSenderNumber();
        this.userGroupService = userGroupService;
        this.restTemplate = restTemplate;

        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("api-key", batchConfiguration.getApiKey());
    }

    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.isEmpty()) {
            return;
        }

        List<String> smsMessageList = checker.getSmsMessage();

        for(String message : smsMessageList) {
            for (String receiver : receivers) {
                try {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("sender", senderNumber);
                    parameters.put("receiver", receiver);
                    parameters.put("text", message);
                    parameters.put("countryCode", "82");
                    parameters.put("type", "sms");

                    HttpEntity httpEntity = new HttpEntity(parameters, headers);
                    ResponseEntity<Map> response = this.restTemplate.exchange(smsServerUrl, HttpMethod.POST, httpEntity, Map.class);

                    if (response.getStatusCode() != HttpStatus.OK) {
                        logger.error("fail send sms message for alarm. response message : {}", response.getBody().toString());
                    }
                } catch (HttpStatusCodeException ex ) {
                    logger.error("fail send sms message for alarm. reponseBody message: {}", ex.getResponseBodyAsString(), ex);
                } catch (Exception ex) {
                    logger.error("fail send sms message for alarm. Caused: {}", ex.getMessage(), ex);
                }
            }
        }
    }
}
