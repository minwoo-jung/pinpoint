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
package com.navercorp.pinpoint.manager.security;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class NaverUserInformationAcquirer implements UserInformationAcquirer {

    private static final String EMPTY_NAME = "";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{pinpointManagerProps['security.header.key.userId']}")
    private String userIdHeaderName;

    @Override
    public String getUserIdHeaderName() {
        return userIdHeaderName;
    }

    @Override
    public String acquireUserId(HttpServletRequest request) {
        return request.getHeader(userIdHeaderName);
    }

    @Override
    public String acquireUserId(ServerHttpRequest request) {
        final List<String> headerList = request.getHeaders().get(userIdHeaderName);

        if (CollectionUtils.isEmpty(headerList)) {
            return EMPTY_NAME;
        }

        return headerList.get(0);
    }

    @Override
    public String acquireOrganizationName(HttpServletRequest request) {
        final String userId = request.getHeader(userIdHeaderName);
        return extractOrganizationName(userId);
    }

    @Override
    public String acquireOrganizationName(ServerHttpRequest request) {
        final List<String> headerList = request.getHeaders().get(userIdHeaderName);
        if (CollectionUtils.isEmpty(headerList)) {
            return EMPTY_NAME;
        }

        final String userId = headerList.get(0);
        return extractOrganizationName(userId);
    }

    private String extractOrganizationName(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return EMPTY_NAME;
        }
        if (userId.length() == 7) {
            return userId.substring(0, 2);
        }
        if (userId.length() == 8) {
            return userId.substring(0, 3);
        }
        logger.warn("Unexpected userId : {}", userId);
        if (userId.length() <= 2) {
            return EMPTY_NAME;
        }
        return userId.substring(0, 2);
    }

    @Override
    public boolean validCheckHeader(String userId, String organizationName) {
        if (org.springframework.util.StringUtils.isEmpty(userId)) {
            return false;
        }
        if (org.springframework.util.StringUtils.isEmpty(organizationName)) {
            return false;
        }

        return true;
    }

}
