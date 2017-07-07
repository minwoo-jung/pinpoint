/*
 * Copyright 2014 NAVER Corp.
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
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.nhncorp.lucy.net.call.Fault;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.call.ReturnValue;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class NaverAlarmMessageSender implements AlarmMessageSender {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("#{batchProps['pinpoint.url']}")
    private String pinpointUrl;
    
    @Value("#{batchProps['batch.server.env']}")
    private String batchEnv;

    
    @Autowired
    private UserGroupService userGroupService;
    
    // Email config
    @Value("#{batchProps['alarm.mail.url']}")
    private String emailServerUrl;
    private static final String QUOTATATION = "\"";
    private static final String ADDR_SEPARATOR = ";";
    private static final String SENDER_EMAIL_ADDRESS = "<dl_labs_p_pinpoint@navercorp.com>";
    private static final String EMAIL_SERVICE_ID = "pinpoint";
    private static final String OPTION = "version=1;mimeCharset=utf-8;debugMode=false";
    
    // SMS config
    @Value("#{batchProps['alarm.sms.url']}")
    private String smsServerUrl;
    @Value("#{batchProps['alarm.sms.serviceId']}")
    private String smsServiceID;
        
    private static final String SENDER_NUMBER = "0317844499";
    
    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
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
    
    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        NpcConnectionFactory factory = new NpcConnectionFactory();
        factory.setBoxDirectoryServiceHostName(emailServerUrl);
        factory.setCharset(StandardCharsets.UTF_8);
        factory.setLightWeight(true);

        NpcHessianConnector connector = null;

        try {
            connector = factory.create();
            Object[] params = createSendMailParams(checker, sequenceCount);
            InvocationFuture future = connector.invoke(null, "send", params);
            future.await();
            Reply reply = (Reply) future.getReturnValue();
            
            if (reply instanceof ReturnValue) {
                Object result = ((ReturnValue) reply).get();
                logger.debug("MessageId:{}", result);
            } else if (reply instanceof Fault) {
                Fault fault = (Fault) reply;
                logger.warn("Unexpected result:code={}, message={}", fault.getCode(), fault.getMessage());
            } else {
                logger.warn("Unexpected clazz({}).", reply.getClass());
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
    }

    private Object[] createSendMailParams(AlarmChecker checker, int sequenceCount) {
        AlarmMailTemplate mailTemplate = new AlarmMailTemplate(checker, pinpointUrl, batchEnv);
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());
        String subject = mailTemplate.createSubject() + " #" + sequenceCount;
        logger.info("send email : {}", subject);
        return new Object[] { EMAIL_SERVICE_ID, OPTION, SENDER_EMAIL_ADDRESS, "", joinAddresses(receivers), subject, mailTemplate.createBody()};
    }

    private String joinAddresses(List<String> addresses) {
        return StringUtils.join(addresses, ADDR_SEPARATOR);
    }
}
