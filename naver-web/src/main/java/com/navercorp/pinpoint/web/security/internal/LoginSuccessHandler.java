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

import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author minwoo.jung
 */
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static String DEFAULT_TARGET_URL = "/";

    private final String secretKey;

    public LoginSuccessHandler(String secretKey) {
        super(DEFAULT_TARGET_URL);

        if (StringUtils.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey must is not empty.");
        }

        this.secretKey = secretKey;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Cookie cookie = JwtCookieCreater.createJwtCookie(secretKey);
        response.addCookie(cookie);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
