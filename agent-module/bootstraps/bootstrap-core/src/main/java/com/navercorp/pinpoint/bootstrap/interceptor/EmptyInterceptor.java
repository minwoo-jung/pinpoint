/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public class EmptyInterceptor implements StaticAroundInterceptor, AroundInterceptor, AroundInterceptor0, AroundInterceptor1, AroundInterceptor2, AroundInterceptor3, AroundInterceptor4, AroundInterceptor5, ApiIdAwareAroundInterceptor {

    public static Interceptor EMPTY = new EmptyInterceptor();

    public static Interceptor empty() {
        return EMPTY;
    }

    private EmptyInterceptor() {
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

    public static String defaultString(final Object object) {
        return String.valueOf(object);
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
    }

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target) {

    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target, Object arg0) {

    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target, Object arg0, Object arg1) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target, Object arg0, Object arg1, Object arg2) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object arg2, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target, Object arg0, Object arg1, Object arg2, Object arg3) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object result, Throwable throwable) {

    }

    @Override
    public void before(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object result, Throwable throwable) {

    }
}
