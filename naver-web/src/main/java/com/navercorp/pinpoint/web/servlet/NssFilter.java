/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author HyunGil Jeong
 */
public class NssFilter implements Filter {

    public static final String UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY = "errorCode";
    public static final int UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE = HttpStatus.SC_MOVED_TEMPORARILY;
    public static final String UNAUTHORIZED_RESPONSE_REDIRECT_KEY = "redirect";
    public static final String UNAUTHORIZED_RESPONSE_REDIRECT_VALUE = "/not_authorized.html";

    public static final Map<String, Object> UNAUTHORIZED_RESPONSE = new HashMap<>();

    static {
        UNAUTHORIZED_RESPONSE.put(UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY, UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE);
        UNAUTHORIZED_RESPONSE.put(UNAUTHORIZED_RESPONSE_REDIRECT_KEY, UNAUTHORIZED_RESPONSE_REDIRECT_VALUE);
    }

    private FilterConfig filterConfig;

    @Value("#{pinpointWebProps['nss.user.header.key'] ?: 'SSO_USER'}")
    private String userHeaderKey;

    @Value("#{pinpointWebProps['nss.override.id'] ?: ''}")
    private String overrideIdVal;

    @Value("#{pinpointWebProps['nss.corportaion.prefix'] ?: ''}")
    private String acceptedCorporationPrefixes;

    @Value("#{pinpointWebProps['nss.user.type'] ?: ''}")
    private String acceptedUserTypes;

    private Set<String> overrideIds;
    private List<String> acceptedPrefixes;

    private ObjectMapper objectMapper;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(this.overrideIdVal)) {
            this.overrideIds = Collections.emptySet();
        } else {
            String[] overrideIds = this.overrideIdVal.split(",");
            if (overrideIds.length == 0) {
                this.overrideIds = Collections.emptySet();
            } else {
                this.overrideIds = new HashSet<>(overrideIds.length);
                for (String overrideId : overrideIds) {
                    this.overrideIds.add(overrideId.toUpperCase());
                }
            }
        }
        if (StringUtils.isEmpty(this.acceptedCorporationPrefixes)) {
            this.acceptedPrefixes = Collections.emptyList();
        } else {
            this.acceptedPrefixes = new ArrayList<>();
            final String[] prefixes = this.acceptedCorporationPrefixes.split(",");
            final String[] types = this.acceptedUserTypes.split(",");
            if (types.length == 0) {
                for (String prefix : prefixes) {
                    this.acceptedPrefixes.add(prefix);
                }
            } else {
                for (String prefix : prefixes) {
                    for (String type : types) {
                        this.acceptedPrefixes.add(prefix + type);
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userId = httpServletRequest.getHeader(userHeaderKey);
        if (userId == null) {
            chain.doFilter(request, response);
        } else {
            if (isAllowed(userId.toUpperCase())) {
                chain.doFilter(request, response);
            } else {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(UNAUTHORIZED_RESPONSE));
            }
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    private boolean isAllowed(String userId) {
        if (this.overrideIds.contains(userId)) {
            return true;
        }
        if (CollectionUtils.isEmpty(this.acceptedPrefixes)) {
            return true;
        } else {
            for (String acceptedPrefix : this.acceptedPrefixes) {
                if (userId.startsWith(acceptedPrefix)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
