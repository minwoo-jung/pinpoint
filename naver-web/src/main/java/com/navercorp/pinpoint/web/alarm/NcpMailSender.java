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
import com.nhncorp.lucy.net.call.Fault;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.call.ReturnValue;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class NcpMailSender implements MailSender {

    private static final String SENDER_EMAIL_ADDRESS = "<dl_labs_p_pinpoint@navercorp.com>";
    private static final String EMAIL_SERVICE_ID = "pinpoint";
    private static final String OPTION = "version=1;mimeCharset=utf-8;debugMode=false";
    private static final String ADDR_SEPARATOR = ";";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String pinpointUrl;
    private String batchEnv;
    private String emailServerUrl;
    private UserGroupService userGroupService;

    public NcpMailSender(NaverBatchConfiguration batchConfiguration, UserGroupService userGroupService) {
        Assert.notNull(batchConfiguration, "batchConfiguration must not be null");
        Assert.notNull(userGroupService, "userGroupService must not be null");

        this.userGroupService = userGroupService;
        this.pinpointUrl = batchConfiguration.getPinpointUrl();
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.emailServerUrl = batchConfiguration.getEmailServerUrl();
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
