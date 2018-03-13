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
package com.navercorp.pinpoint.web.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
@Component
public class NssAuthenticationFilter extends OncePerRequestFilter {

    private static final String SSO_USER = "SSO_USER";
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserGroupService userGroupService;
    
    @Autowired
    ApplicationConfigService configService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (StringUtils.isEmpty(request.getHeader(SSO_USER))) {
            HttpServletResponse httpServletResponse = response; 
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.getWriter().print("{\"error code\" : \"" + 401 + "\"," + "\"error message\" : \"" + "you should login." + "\"}");
            return;
        }

        final String userId = request.getHeader(SSO_USER);

        StaticOrganizationInfoAllocator.allocate(userId);

        User user = userService.selectUserByUserId(userId);
        List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userId);
        boolean pinpointManager = isManager(userId);
        Authentication authentication;
        
        if (user != null) {
            authentication = new PinpointAuthentication(user.getUserId(), user.getName(), userGroups, null, true, pinpointManager);
        } else {
            authentication = new PinpointAuthentication();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isManager(String userId) {
        List<User> user = configService.selectManagerByUserId(userId);

        if (user.size() > 0) {
            return true;
        }

        return false;
    }


}
