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
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * @author minwoo.jung
 */
public class RegisterInterceptor implements AroundInterceptor {

//    private UriStatMetricRegistry urlStatMetricRegistry;

//    public RegisterInterceptor(UrlStatMetricRegistry urlStatMetricRegistry) {
//    public RegisterInterceptor() {
//        this.urlStatMetricRegistry = new UriStatMetricRegistry();
//    }

    @Override
    public void before(Object target, Object[] args) {
        System.out.println("=============method call=================");
        for (int i =0 ; i < 3 ; i++) {
            System.out.println(args[i]);
        }
        if (args[1] instanceof RequestMappingInfo) {
//            urlStatMetricRegistry.addRequestMappingInfo((RequestMappingInfo)args[1]);
        }
        System.out.println("========================================");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
