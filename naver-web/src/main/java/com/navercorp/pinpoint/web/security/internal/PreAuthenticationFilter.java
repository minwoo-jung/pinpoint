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
package com.navercorp.pinpoint.web.security.internal;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.security.LoginStaticString;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author minwoo.jung
 */
public class PreAuthenticationFilter extends OncePerRequestFilter {

    private final static String ERROR_MESSAGE_AUTHENTICATION = "{\"error code\" : \"401\", \"error message\" : \"error occurred in Authentication process.\"}";
    private final static String EMPTY = "";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String secretKey;

    public PreAuthenticationFilter(String secretKey) {
        if (StringUtils.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey must is not empty.");
        }
        this.secretKey = secretKey;
    }

    @Autowired
    private InternalAuthenticationProvider internalAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String url = request.getRequestURI();
            if (url.contains(LoginStaticString.LOGIN_URL) || url.contains(LoginStaticString.LOGIN_PROCESSING_URL)) {
                chain.doFilter(request, response);
                return;
            }

            final String token = getJWTToken(request);
            if (StringUtils.isEmpty(token)) {
                chain.doFilter(request, response);
                return;
            }

            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            String userId = (String) claims.get(JwtCookieCreater.USER_ID);
            String organizationName = (String) claims.get(JwtCookieCreater.ORGANIZATION_NAME);
            Date expirationTime = claims.getExpiration();

            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(organizationName)) {
                logger.error("can not get userId or organizationName in cookie.");
                chain.doFilter(request, response);
                return;
            }

            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis >= expirationTime.getTime()) {
                logger.error("current time exceeds the expireation time. userId : {}, organizationName : {},  currentTime : {}, expirationTime : {}", userId, organizationName, new Date(), expirationTime);
                chain.doFilter(request, response);
                return;
            }


            internalAuthenticationProvider.allocatePaaSOrganizationInfoRequestScope(userId, organizationName);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(internalAuthenticationProvider.createPinpointAuthentication(userId));
            SecurityContextHolder.setContext(context);
            updateExpiratimeTime(response, currentTimeMillis, expirationTime.getTime());

            chain.doFilter(request, response);
        } catch (AuthenticationException exception) {
            logger.error(exception.getMessage(), exception);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().print(ERROR_MESSAGE_AUTHENTICATION);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void updateExpiratimeTime(HttpServletResponse response, long currentTime, long expirationTime) {
        if (expirationTime - currentTime > JwtCookieCreater.UPDATE_INTERVAL_JWT_COOKIE_MILLIS) {
            return;
        }

        Cookie cookie = JwtCookieCreater.createJwtCookie(secretKey);
        response.addCookie(cookie);
    }

    private String getJWTToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isEmpty(cookies)) {
            return EMPTY;
        }

        for (Cookie cookie : cookies) {
            if (JwtCookieCreater.JWT_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return EMPTY;
    }
}
