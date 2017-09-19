package com.navercorp.pinpoint.web.security;

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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


public class LocalAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String SSO_USER = "SSO_USER";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String userId = (String) authentication.getPrincipal();
            if (!StringUtils.isEmpty(userId)) {
                CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);
                customRequest.putHeader(SSO_USER, userId);
                chain.doFilter(customRequest, response);
                return;
            }

        }
        
        chain.doFilter(request, response);
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
