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

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.plugin.nelo.UsingNeloAppenderAccessor;

/**
 * @author minwoo.jung
 */
public class LoggerOnMessageMethodInterceptor implements AroundInterceptor0 {

    private final TraceContext traceContext;

    public LoggerOnMessageMethodInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        if (Boolean.FALSE.equals(((UsingNeloAppenderAccessor) target)._$PINPOINT$_getUsingNeloAppender().get())) {
            return;
        }

        Trace trace = traceContext.currentTraceObject();

        if (trace != null) {
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordLogging(LoggingInfo.LOGGED);
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
