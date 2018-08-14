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

package com.navercorp.pinpoint.collector.namespace;

import com.navercorp.pinpoint.collector.service.MetadataService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.security.SecurityConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HyunGil Jeong
 */
public class PinpointServerNameSpaceInfoPropagateInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, NameSpaceInfo> nameSpaceInfoCache = new ConcurrentHashMap<>();

    private final MetadataService metadataService;

    private final boolean useDefaultNameSpaceInfo;

    public PinpointServerNameSpaceInfoPropagateInterceptor(boolean useDefaultNameSpaceInfo) {
        this(new MetadataService() {
            @Override
            public PaaSOrganizationKey selectPaaSOrganizationkey(String licenseKey) {
                return null;
            }

            @Override
            public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
                return null;
            }
        }, useDefaultNameSpaceInfo);
    }

    public PinpointServerNameSpaceInfoPropagateInterceptor(MetadataService metadataService, boolean useDefaultNameSpaceInfo) {
        this.metadataService = Objects.requireNonNull(metadataService, "metadataService must not be null");
        this.useDefaultNameSpaceInfo = useDefaultNameSpaceInfo;
    }

    public void aroundAdvice(ProceedingJoinPoint joinPoint, PinpointServer pinpointServer) throws Throwable {
        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();
        NameSpaceInfo nameSpaceInfo = getNameSpaceInfo(channelProperties);
        if (nameSpaceInfo == null) {
            if (useDefaultNameSpaceInfo) {
                nameSpaceInfo = NameSpaceInfo.DEFAULT;
            } else {
                logger.error("Unable to retrieve NameSpaceInfo from : {}, channelProperties : {}", pinpointServer, channelProperties);
                throw new IllegalStateException("Cannot find NameSpaceInfo");
            }
        }

        try {
            beforeInterceptor(nameSpaceInfo);
            joinPoint.proceed();
        } finally {
            afterInterceptor();
        }
    }

    private void beforeInterceptor(NameSpaceInfo nameSpaceInfo) {
        RequestAttributes requestAttributes = new RequestAttributes(new HashMap<>());
        requestAttributes.setAttribute(NameSpaceInfo.NAMESPACE_INFO, nameSpaceInfo);
        RequestContextHolder.setAttributes(requestAttributes);

        if (logger.isDebugEnabled()) {
            logger.debug("initialized RequestContextHolder for NameSpaceInfo : {}", nameSpaceInfo);
        }
    }

    private void afterInterceptor() {
        if (logger.isDebugEnabled()) {
            RequestAttributes attributes = RequestContextHolder.currentAttributes();
            NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) attributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
            logger.debug("reset RequestContextHolder for NamespaceInfo : {}", nameSpaceInfo);
        }

        RequestContextHolder.resetAttributes();
    }

    private NameSpaceInfo getNameSpaceInfo(Map<Object, Object> channelProperties) {
        String licenseKey = (String) channelProperties.get(SecurityConstants.KEY_LICENSE_KEY);
        if (licenseKey == null) {
            return null;
        }
        NameSpaceInfo nameSpaceInfo = nameSpaceInfoCache.get(licenseKey);
        if (nameSpaceInfo != null) {
            return nameSpaceInfo;
        }

        NameSpaceInfo newNameSpaceInfo = getNameSpaceInfoFromService(licenseKey);
        if (newNameSpaceInfo == null) {
            return null;
        }
        NameSpaceInfo previous = nameSpaceInfoCache.putIfAbsent(licenseKey, newNameSpaceInfo);
        if (previous != null) {
            return previous;
        }
        return newNameSpaceInfo;
    }

    private NameSpaceInfo getNameSpaceInfoFromService(String licenseKey) {
        PaaSOrganizationKey organizationKey = metadataService.selectPaaSOrganizationkey(licenseKey);
        if (organizationKey == null) {
            logger.debug("PaaSOrganizationKey does not exist for licenseKey : {}", licenseKey);
            return null;
        }

        PaaSOrganizationInfo paaSOrganizationInfo = metadataService.selectPaaSOrganizationInfo(organizationKey.getOrganization());
        if (paaSOrganizationInfo == null) {
            logger.debug("PaaSOrganizationInfo does not exist for organization : {}", organizationKey.getOrganization());
            return null;
        }

        String organization = paaSOrganizationInfo.getOrganization();
        String databaseName = paaSOrganizationInfo.getDatabaseName();
        String hbaseNameSpace = paaSOrganizationInfo.getHbaseNameSpace();

        if (StringUtils.hasText(organization) && StringUtils.hasText(databaseName) && StringUtils.hasText(hbaseNameSpace)) {
            return new NameSpaceInfo(organization, databaseName, hbaseNameSpace);
        }
        return null;
    }
}
