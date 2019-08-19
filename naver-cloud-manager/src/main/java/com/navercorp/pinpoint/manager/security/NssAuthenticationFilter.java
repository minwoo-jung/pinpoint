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
package com.navercorp.pinpoint.manager.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author minwoo.jung
 */
public class NssAuthenticationFilter extends OncePerRequestFilter {

    private final static String ERROR_MESSAGE_USER_INFO_EMPTY = "{\"error code\" : \"401\", \"error message\" : \"error occurred in login process. May be userId or organization is empty.\"}";
    private final static String ERROR_MESSAGE_NOT_FOUND_ORGANIZATION = "{\"error code\" : \"401\", \"error message\" : \"error occurred in finding organization.\"}";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected UserInformationAcquirer userInformationAcquirer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String userId = userInformationAcquirer.acquireUserId(request);
        final String organizationName = userInformationAcquirer.acquireOrganizationName(request);
        if (userInformationAcquirer.validCheckHeader(userId, organizationName) == false) {
            logger.error("error occurred in checking userId({}), organizationName({}). Maybe userId or organizationName is null.", userId, organizationName);
            responseForUnauthorized(response, ERROR_MESSAGE_USER_INFO_EMPTY);
            return;
        }


        Authentication authentication = new PinpointAuthentication(userId, userId);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void responseForUnauthorized(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(errorMessage);
    }

}
