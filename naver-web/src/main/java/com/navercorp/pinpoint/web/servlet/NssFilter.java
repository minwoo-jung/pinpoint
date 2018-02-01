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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.service.NssAuthService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author HyunGil Jeong
 */
public class NssFilter implements Filter {

    public static final String UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY = "errorCode";
    public static final int UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE = HttpStatus.SC_MOVED_TEMPORARILY;
    public static final String UNAUTHORIZED_RESPONSE_REDIRECT_KEY = "redirect";
    public static final String UNAUTHORIZED_RESPONSE_REDIRECT_VALUE = "/not_authorized.html";

    private static final Map<String, Object> UNAUTHORIZED_RESPONSE = new HashMap<>();

    static {
        UNAUTHORIZED_RESPONSE.put(UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY, UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE);
        UNAUTHORIZED_RESPONSE.put(UNAUTHORIZED_RESPONSE_REDIRECT_KEY, UNAUTHORIZED_RESPONSE_REDIRECT_VALUE);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String userHeaderKey;
    private final NssAuthService nssAuthService;
    private final String unauthorizedResponse;
    private volatile List<String> authorizedPrefixes = Collections.emptyList();
    private volatile Set<String> overrideIds = Collections.emptySet();

    public NssFilter(String userHeaderKey, NssAuthService nssAuthService, ObjectMapper objectMapper) {
        Assert.hasLength(userHeaderKey, "userHeaderKey must not be empty");
        Assert.notNull(nssAuthService, "nssAuthService must not be null");
        Assert.notNull(objectMapper, "objectMapper must not be null");
        this.userHeaderKey = userHeaderKey;
        this.nssAuthService = nssAuthService;
        this.unauthorizedResponse = createUnauthorizedResponse(objectMapper);
    }

    private String createUnauthorizedResponse(ObjectMapper objectMapper) {
        Map<String, Object> unauthorizedResponseMap = new HashMap<>();
        unauthorizedResponseMap.put(UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY, UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE);
        unauthorizedResponseMap.put(UNAUTHORIZED_RESPONSE_REDIRECT_KEY, UNAUTHORIZED_RESPONSE_REDIRECT_VALUE);
        try {
            return objectMapper.writeValueAsString(unauthorizedResponseMap);
        } catch (JsonProcessingException e) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"").append(UNAUTHORIZED_RESPONSE_ERROR_CODE_KEY).append("\"").append(":");
            sb.append(UNAUTHORIZED_RESPONSE_ERROR_CODE_VALUE).append(",");
            sb.append("\"").append(UNAUTHORIZED_RESPONSE_REDIRECT_KEY).append("\"").append(":");
            sb.append("\"").append(UNAUTHORIZED_RESPONSE_REDIRECT_VALUE).append("\"");
            sb.append("}");
            return sb.toString();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @PostConstruct
    public void init() {
        update();
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
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(this.unauthorizedResponse);
            }
        }
    }

    @Override
    public void destroy() {
    }

    public void update() {
        List<String> authorizedPrefixes = new ArrayList<>();
        for (String authorizedPrefix : nssAuthService.getAuthorizedPrefixes()) {
            authorizedPrefixes.add(authorizedPrefix.toUpperCase());
        }
        logger.debug("Updating authorized prefixes from : {} to {}", this.authorizedPrefixes, authorizedPrefixes);
        this.authorizedPrefixes = Collections.unmodifiableList(authorizedPrefixes);

        Set<String> overrideIds = new HashSet<>();
        for (String overrideId : nssAuthService.getOverrideUserIds()) {
            overrideIds.add(overrideId.toUpperCase());
        }
        logger.debug("Updating override ids from : {} to {}", this.overrideIds, overrideIds);
        this.overrideIds = Collections.unmodifiableSet(overrideIds);
    }

    public String getUnauthorizedResponse() {
        return unauthorizedResponse;
    }

    boolean isAllowed(String userId) {
        if (this.overrideIds.contains(userId)) {
            return true;
        }
        if (CollectionUtils.isEmpty(this.authorizedPrefixes)) {
            return true;
        } else {
            for (String authorizedPrefix : this.authorizedPrefixes) {
                if (userId.startsWith(authorizedPrefix)) {
                    return true;
                }
            }
            return false;
        }
    }
}
