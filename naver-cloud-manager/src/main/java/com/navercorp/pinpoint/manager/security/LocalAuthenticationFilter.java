package com.navercorp.pinpoint.manager.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.navercorp.pinpoint.manager.security.AutoLoginAuthenticationFilter.CustomHttpServletRequest;


public class LocalAuthenticationFilter extends OncePerRequestFilter {

    @Value("#{pinpointManagerProps['security.header.key.userId']}")
    private String userIdHeaderName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String userId = (String) authentication.getPrincipal();
            if (!StringUtils.isEmpty(userId)) {
                CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);
                customRequest.putHeader(userIdHeaderName, userId);
                chain.doFilter(customRequest, response);
                return;
            }

        }

        chain.doFilter(request, response);
    }
}