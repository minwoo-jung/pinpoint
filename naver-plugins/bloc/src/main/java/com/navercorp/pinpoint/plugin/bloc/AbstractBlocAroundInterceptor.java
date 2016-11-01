/*
 *  Copyright 2015 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @Author Taejin Koo
 */
public abstract class AbstractBlocAroundInterceptor implements AroundInterceptor {

    protected static final int MAX_EACH_PARAMETER_SIZE = 64;
    protected static final int MAX_ALL_PARAMETER_SIZE = 512;

    protected final BlocMethodDescriptor blocMethodApiTag = new BlocMethodDescriptor();

    protected final PLogger logger;
    protected final boolean isDebug;

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;

    protected final boolean traceRequestParam;

    protected AbstractBlocAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, Class<? extends AbstractBlocAroundInterceptor> childClazz) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.logger = PLoggerFactory.getLogger(childClazz);
        this.isDebug = logger.isDebugEnabled();

        BlocPluginConfig config = new BlocPluginConfig(traceContext.getProfilerConfig());
        traceRequestParam = config.isBlocTraceRequestParam();

        traceContext.cacheApi(blocMethodApiTag);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validateArgument(args)) {
            return;
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            // TODO STATDISABLE this logic was added to disable statistics tracing
            if (!trace.canSampled()) {
                return;
            }
            doInBeforeTrace(trace, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract boolean validateArgument(final Object[] args);

    protected abstract Trace createTrace(final Object target, final Object[] args);

    protected abstract void doInBeforeTrace(final Trace trace, Object target, final Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validateArgument(args)) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        // ------------------------------------------------------
        try {
            doInAfterTrace(trace, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            deleteTrace(trace, target, args, result, throwable);
        }
    }

    protected abstract void doInAfterTrace(final Trace trace, final Object target, final Object[] args, final Object result, Throwable throwable);

    protected void deleteTrace(final Trace trace, final Object target, final Object[] args, final Object result, Throwable throwable) {
        trace.close();
    }

}
