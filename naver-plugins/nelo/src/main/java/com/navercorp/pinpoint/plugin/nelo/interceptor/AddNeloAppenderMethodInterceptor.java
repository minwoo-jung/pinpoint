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
package com.navercorp.pinpoint.plugin.nelo.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.nelo.UsingNeloAppenderAccessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class AddNeloAppenderMethodInterceptor implements AroundInterceptor {

    private final List<String> neloAppenderClassNameList;

    public AddNeloAppenderMethodInterceptor(List<String> neloAppenderClassNameList) {
        this.neloAppenderClassNameList = neloAppenderClassNameList;
    }

    @Override
    public void before(Object target, Object[] args) {
        if(args == null || args[0] == null) {
            return;
        }

        final String className = args[0].getClass().getName();

        for (String neloAppenderClassName : neloAppenderClassNameList) {
            if (className.equals(neloAppenderClassName)) {
                setUsingNeloAppender(target, true);
                return;
            }
        }

        setUsingNeloAppender(target, false);
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

    public void setUsingNeloAppender(Object target, boolean usingNelo) {
        if (target instanceof UsingNeloAppenderAccessor) {
            AtomicBoolean isUsingNeloAppender = ((UsingNeloAppenderAccessor) target)._$PINPOINT$_getUsingNeloAppender();

            if (isUsingNeloAppender == null) {
                isUsingNeloAppender = new AtomicBoolean();
                ((UsingNeloAppenderAccessor) target)._$PINPOINT$_setUsingNeloAppender(isUsingNeloAppender);
            }

            isUsingNeloAppender.set(usingNelo);
        }
    }
}
