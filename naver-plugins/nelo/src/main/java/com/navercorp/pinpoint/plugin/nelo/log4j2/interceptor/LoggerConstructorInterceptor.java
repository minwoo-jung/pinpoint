/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nelo.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.nelo.UsingNeloAppenderAccessor;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.config.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class LoggerConstructorInterceptor implements AroundInterceptor {

    private final List<String> neloAppenderClassNameList;

    public LoggerConstructorInterceptor(List<String> neloAppenderClassNameList) {
        this.neloAppenderClassNameList = neloAppenderClassNameList;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if(args == null || args[0] == null || args[1] == null) {
            return;
        }
        if (args[0] instanceof LoggerContext == false) {
            return;
        }

        if (args[1] instanceof String == false) {
            return;
        }

        final Configuration configuration = ((LoggerContext)args[0]).getConfiguration();
        final String name = (String)args[1];
        final Map<String, Appender> AppenderMapForThisLogger = configuration.getLoggerConfig(name).getAppenders();
        final Map<String, Appender> AppenderMapForAllLogger = configuration.getAppenders();

        for (Appender appender : AppenderMapForThisLogger.values()) {
            if (appender instanceof AsyncAppender) {
                if(containNeloAppender((AsyncAppender) appender, AppenderMapForAllLogger)) {
                    setUsingNeloAppender(target, true);
                    return;
                }
            }

            if (equalsNeloAppender(appender) == true) {
                setUsingNeloAppender(target, true);
                return;
            }
        }

        setUsingNeloAppender(target, false);
    }

    private boolean containNeloAppender(AsyncAppender appender, Map<String,Appender> appenderMap) {
        String[] appenderNames = appender.getAppenderRefStrings();

        if (appenderNames == null || appenderNames.length == 0) {
            return false;
        }

        List<String> appenderNameList = Arrays.asList(appenderNames);
        for (String appenderName : appenderNameList) {
            if (appenderMap.containsKey(appenderName)) {
                if (equalsNeloAppender(appenderMap.get(appenderName))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean equalsNeloAppender(Appender appender) {
        for (String neloAppenderClassName : neloAppenderClassNameList) {
            if (neloAppenderClassName.equals(appender.getClass().getName())) {
                return true;
            }
        }

        return false;
    }

    private void setUsingNeloAppender(Object target, boolean usingNelo) {
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

