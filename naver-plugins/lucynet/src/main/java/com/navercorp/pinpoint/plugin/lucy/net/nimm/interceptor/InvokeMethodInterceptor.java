/**
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetPluginConfig;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetUtils;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 */
public class InvokeMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    private final boolean param;

    // TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고 있음.

    public InvokeMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;

        LucyNetPluginConfig config = new LucyNetPluginConfig(traceContext.getProfilerConfig());
        this.param = config.isNimmParam();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // UUID format을 그대로.
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        currentTransaction.setAttachment(nextId);

        // TODO protocol은 어떻게 표기하지???
        String nimmAddress = "";
        if (target instanceof NimmAddressAccessor) {
            nimmAddress = ((NimmAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }
        if (StringUtils.isEmpty(nimmAddress)) {
            recorder.recordDestinationId("unknown");
        } else {
            recorder.recordDestinationId(nimmAddress);
        }

        // final long timeoutMillis = (Long) args[0];
        String objectName = null;
        if (args != null && args.length >= 2 && args[1] instanceof String) {
            objectName = (String) args[1];
        }

        String methodName = null;
        if (args != null && args.length >= 3 && args[2] instanceof String) {
            methodName = (String) args[2];
        }

        recorder.recordAttribute(LucyNetConstants.NIMM_URL, bindingNimmUrl(nimmAddress, objectName, methodName));

        if (this.param && args != null && args.length >= 4 && args[3] instanceof Object[]) {
            final Object[] params = (Object[]) args[3];
            recorder.recordAttribute(LucyNetConstants.NIMM_PARAM, LucyNetUtils.getParameterAsString(params, 64, 512));
        }

        recorder.recordServiceType(LucyNetConstants.NIMM_CLIENT);
    }

    private String bindingNimmUrl(String nimmAddress, String objectName, String methodName) {
        if (objectName != null) {
            return nimmAddress + "/" + objectName + "/" + methodName;
        } else {
            return nimmAddress + "/" + methodName;
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            if (isAsynchronousInvocation(target, args, result, throwable)) {
                // set asynchronous trace
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                ((AsyncTraceIdAccessor)result)._$PINPOINT$_setAsyncTraceId(asyncTraceId);
                if (isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
                }
            }

        } finally {
            trace.traceBlockEnd();
        }
    }
    
    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(result instanceof AsyncTraceIdAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncTraceIdAccessor.class.getName());
            return false;
        }

        return true;
    }
}