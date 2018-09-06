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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.security.SecurityConstants;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class NaverTcpDataSender extends TcpDataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HeaderEntity headerEntity;

    public NaverTcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, HeaderTBaseSerializer serializer, String licenseKey) {
        super(name, host, port, clientFactory, serializer);

        Assert.requireNonNull(licenseKey, "licenseKey must not be null");

        Map<String, String> headerEntityData = new HashMap<String, String>(1);
        headerEntityData.put(SecurityConstants.KEY_LICENSE_KEY, licenseKey);

        this.headerEntity = new HeaderEntity(headerEntityData);
    }

    @Override
    protected byte[] serialize(HeaderTBaseSerializer serializer, TBase tBase) {
        Assert.requireNonNull(serializer, "serializer must not be null");
        Assert.requireNonNull(tBase, "tBase must not be null");

        try {
            return serializer.serialize(tBase, headerEntity);
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Serialize " + tBase + " failed. Error:" + e.getMessage(), e);
            }
        }

        return null;
    }

}
