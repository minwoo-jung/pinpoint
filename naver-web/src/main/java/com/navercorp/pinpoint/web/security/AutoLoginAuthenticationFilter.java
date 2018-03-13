/**
 * <p/>
 * Copyright NAVER Corp.
 * http://yobi.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
@Component
public class AutoLoginAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String SSO_USER = "SSO_USER";
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private ApplicationConfigService configService;

    private String userId;
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
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
        
        CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);
        customRequest.putHeader(SSO_USER, userId);
        try {
            chain.doFilter(customRequest, response);
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
    
    private static class CustomHttpServletRequest extends HttpServletRequestWrapper {
        private final Map<String, String> headers;
     
        public CustomHttpServletRequest(HttpServletRequest request){
            super(request);
            this.headers = new HashMap<String, String>();
        }
        
        public void putHeader(String name, String value){
            this.headers.put(name, value);
        }
     
        public String getHeader(String name) {
            String headerValue = headers.get(name);
            if (headerValue != null){
                return headerValue;
            }
            return ((HttpServletRequest) getRequest()).getHeader(name);
        }
     
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<String>(headers.keySet());
            Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
            while (e.hasMoreElements()) {
                String n = e.nextElement();
                set.add(n);
            }
     
            return Collections.enumeration(set);
        }
        
        public Enumeration<String> getHeaders(String name) {
            Set<String> set = new HashSet<String>();
            String headerValue = headers.get(name);
            if (headerValue != null){
                set.add(headerValue);
            }
            
            Enumeration<String> headers = super.getHeaders(name);
            while (headers.hasMoreElements()) {
                String value = headers.nextElement();
                set.add(value);
            }
            
            return Collections.enumeration(set);
        };
        
    }

}
