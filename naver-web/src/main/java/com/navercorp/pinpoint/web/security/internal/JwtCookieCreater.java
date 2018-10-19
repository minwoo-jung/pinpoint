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

import com.navercorp.pinpoint.web.namespace.vo.PaaSOrganizationInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.JwtMap;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public final class JwtCookieCreater {

    //TODO : (minwoo) 별도 설정을 빼야한다.
    public static final String JWT_COOKIE_NAME = "jwt";
    public static final String USER_ID = "userId";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final String JWT_COOKIE_PATH = "/";
    //TODO : (minwoo) 시크릿키 생성
    public static final String SECRET_KEY = "ThisIsASecret";

    public static final int UPDATE_INTERVAL_JWT_COOKIE_MILLIS = 43200; // 12 hours
    private static final int EXPIRATION_TIME_SECONDS = 86400; //1 day
    private static final long EXPIRATION_TIME_MILLIS = 86_400_000; //1 day
//    public static final long UPDATE_INTERVAL_JWT_COOKIE_MILLIS = 43_200_000; // 12 hours
//    private static final int EXPIRATION_TIME_SECONDS = 300; //5 min
//    private static final long EXPIRATION_TIME_MILLIS = 300_000; //5 min

    public static Cookie createJwtCookie() {
        long expTime = System.currentTimeMillis() + EXPIRATION_TIME_MILLIS;

        String jwt = Jwts.builder()
            .setSubject("pinpointAuthentication")
            .setClaims(createPayload())
            .setExpiration(new Date(expTime))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();

        Cookie cookie = new Cookie(JWT_COOKIE_NAME, jwt);
        cookie.setPath(JWT_COOKIE_PATH);
        cookie.setMaxAge(EXPIRATION_TIME_SECONDS);

        return cookie;
    }

    private static JwtMap createPayload() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            throw new InternalAuthenticationServiceException("can't not found authencation.");
        }

        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO, RequestAttributes.SCOPE_REQUEST);
        if (Objects.isNull(paaSOrganizationInfo)) {
            throw new InternalAuthenticationServiceException("can't not found paaSOrganizationInfo.");
        }

        JwtMap payload = new JwtMap();
        payload.put(USER_ID, authentication.getPrincipal());
        payload.put(ORGANIZATION_NAME, paaSOrganizationInfo.getOrganization());
        return payload;
    }
}
