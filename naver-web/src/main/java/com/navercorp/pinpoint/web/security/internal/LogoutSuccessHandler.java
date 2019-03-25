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

import com.navercorp.pinpoint.web.security.LoginStaticString;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author minwoo.jung
 */
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    public LogoutSuccessHandler() {
        setDefaultTargetUrl(LoginStaticString.LOGOUT_SUCCESS_URL);
    }

    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Cookie cookie = new Cookie(JwtCookieCreater.JWT_COOKIE_NAME, null);
        cookie.setPath(JwtCookieCreater.JWT_COOKIE_PATH);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        super.onLogoutSuccess(request, response, authentication);
    }
}
