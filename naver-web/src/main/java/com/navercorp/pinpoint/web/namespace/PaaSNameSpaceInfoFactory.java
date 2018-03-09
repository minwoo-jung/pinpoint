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
package com.navercorp.pinpoint.web.namespace;

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.util.BatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

/**
 * @author minwoo.jung
 */
public class PaaSNameSpaceInfoFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean batchServer;

    @Autowired
    @Qualifier("batchNameSpaceInfoHolder")
    NameSpaceInfoHolder batchNameSpaceInfoHolder;

    @Autowired
    @Qualifier("requestNameSpaceInfoHolder")
    NameSpaceInfoHolder requestNameSpaceInfoHolder;

    @Autowired
    public PaaSNameSpaceInfoFactory(BatchConfiguration batchConfiguration) {
        Assert.notNull(batchConfiguration, "batchConfiguration must not be empty");
        batchServer = BatchUtils.decisionBatchServer(batchConfiguration.getBatchServerIp());
    }

    public NameSpaceInfo getNameSpaceInfo() {
        try {
            if (batchServer) {
                NameSpaceInfo nameSpaceInfo = batchNameSpaceInfoHolder.getNameSpaceInfo();
                return nameSpaceInfo;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while get nameSpaceInfo [Thread name : {} ]", Thread.currentThread().getName(), e);
        }

        try {
            NameSpaceInfo nameSpaceInfo = requestNameSpaceInfoHolder.getNameSpaceInfo();
            return nameSpaceInfo;
        } catch (Exception e) {
            logger.error("Exception occurred while get nameSpaceInfo [Thread name : {} ]", Thread.currentThread().getName() ,e);
        }

        throw new RuntimeException("can't get NameSpaceInfo in batchNameSpaceInfoHolder and requestNameSpaceInfoHolder [Thread name : " + Thread.currentThread().getName() + "]");
    }
}
