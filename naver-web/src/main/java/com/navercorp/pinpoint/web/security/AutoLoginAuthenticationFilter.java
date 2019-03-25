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

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * @author minwoo.jung
 */
@Component
public class AutoLoginAuthenticationFilter extends NssAuthenticationFilter {

    private final String userId;

    public AutoLoginAuthenticationFilter(String userId) {
        Objects.requireNonNull(userId, "defaultUserId must not be empty");
        this.userId = userId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);
        customRequest.putHeader(userInformationAcquirer.getUserIdHeaderName(), userId);

        super.doFilterInternal(customRequest, response, chain);
    }

    public static class CustomHttpServletRequest extends HttpServletRequestWrapper {
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
