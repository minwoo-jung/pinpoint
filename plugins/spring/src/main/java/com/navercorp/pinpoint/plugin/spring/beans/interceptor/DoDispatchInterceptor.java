/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatMetricRegistry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author minwoo.jung
 */
public class DoDispatchInterceptor implements AroundInterceptor {

    private final UriStatMetricRegistry uriStatMetricRegistry;

    public DoDispatchInterceptor(UriStatMetricRegistry uriStatMetricRegistry) {
        this.uriStatMetricRegistry = uriStatMetricRegistry;
    }

    @Override
    public void before(Object target, Object[] args) {
        System.out.println("=start=");
        for (int i = 0 ; i < args.length; i++) {
            System.out.println(args[i]);
        }
        System.out.println("=end=");
        if (args != null && args.length > 1) {
            if (args[0] instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest)args[0];
//                request.getAttribute()
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
