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

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author minwoo.jung
 */
public class SessionRequestNameSpaceInfoHolder implements NameSpaceInfoHolder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NameSpaceInfo requestNameSpaceInfo;

    public SessionRequestNameSpaceInfoHolder() {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_SESSION);
        Assert.notNull(paaSOrganizationInfo, "PaaSOrganizationInfo must not be null.");

        String databaseName = paaSOrganizationInfo.getDatabaseName();
        String hbaseNameSpace = paaSOrganizationInfo.getHbaseNameSpace();
        this.requestNameSpaceInfo = new NameSpaceInfo(paaSOrganizationInfo.getUserId(), databaseName, hbaseNameSpace);
    }

    @Override
    public NameSpaceInfo getNameSpaceInfo() throws Exception {
        return requestNameSpaceInfo;
    }
}
