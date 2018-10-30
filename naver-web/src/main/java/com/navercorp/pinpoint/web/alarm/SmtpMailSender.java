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
import org.springframework.util.Assert;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class SmtpMailSender implements MailSender {

    private static final InternetAddress SENDER_EMAIL_ADDRESS;
    private static final InternetAddress[] EMPTY_RECEIVERS = new InternetAddress[0];
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";

    static {
        try {
            SENDER_EMAIL_ADDRESS = new InternetAddress("dl_labs_p_pinpoint@navercorp.com");
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String emailServerUrl;
    private final Properties stmpProperties;
    private final UserGroupService userGroupService;
    private final String pinpointUrl;
    private final String batchEnv;

    public SmtpMailSender(NaverBatchConfiguration batchConfiguration, UserGroupService userGroupService) {
        Assert.notNull(batchConfiguration, "batchConfiguration must not be null");
        Assert.notNull(userGroupService, "userGroupService must not be null");

        this.emailServerUrl = batchConfiguration.getEmailServerUrl();
        this.pinpointUrl = batchConfiguration.getPinpointUrl();
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.userGroupService = userGroupService;

        Properties props = new Properties();
        props.put(MAIL_SMTP_HOST, emailServerUrl);
        this.stmpProperties = props;

    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        try {
            Session session = Session.getDefaultInstance(stmpProperties);
            Message message = new MimeMessage(session);
            message.setFrom(SENDER_EMAIL_ADDRESS);
            message.setRecipients(Message.RecipientType.TO, getReceivers(checker.getuserGroupId()));

            AlarmMailTemplate mailTemplate = new AlarmMailTemplate(checker, pinpointUrl, batchEnv);
            String subject =  mailTemplate.createSubject() + " #" + sequenceCount;
            message.setSubject(subject);
            message.setContent(mailTemplate.createBody(), "text/html");

            logger.info("send email : {}", subject);
            Transport.send(message);
        } catch (Exception e) {
            logger.error("can't send alarm email. {}", checker.getRule(), e);
        }
    }

    private InternetAddress[] getReceivers(String userGroupId) throws AddressException {
        List<String> receivers = userGroupService.selectEmailOfMember(userGroupId);

        if (receivers.size() == 0) {
            return EMPTY_RECEIVERS;
        }

        InternetAddress[] receiverArray = new InternetAddress[receivers.size()];
        int index = 0;
        for (String receiver : receivers) {
            receiverArray[index++] = new InternetAddress(receiver);
        }

        return receiverArray;
    }
}
