/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jeus.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.plugin.common.servlet.util.HttpServletRequestAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.util.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.jeus.JeusConfig;
import com.navercorp.pinpoint.plugin.jeus.JeusConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jaehong.kim
 */
public class ServletWrapperExecuteInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;
    private final ServletRequestListenerInterceptorHelper<HttpServletRequest> servletRequestListenerInterceptorHelper;

    public ServletWrapperExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        final JeusConfig config = new JeusConfig(traceContext.getProfilerConfig());
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        final ParameterRecorder<HttpServletRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<HttpServletRequest>(JeusConstants.JEUS, traceContext, requestAdaptor, config.getExcludeUrlFilter(), parameterRecorder);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(args)) {
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            if (request.getAttribute(JeusConstants.JEUS_ASYNC_OPERATION) != null) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event.");
                }
                // skip
                return;
            }
            this.servletRequestListenerInterceptorHelper.initialized(request, JeusConstants.JEUS_METHOD, this.methodDescriptor);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validate(args)) {
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final HttpServletResponse response = (HttpServletResponse) args[1];
            final int statusCode = getStatusCode(response);
            this.servletRequestListenerInterceptorHelper.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 2) {
            return false;
        }

        if (!(args[0] instanceof HttpServletRequest)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object, Not implemented of javax.servlet.http.HttpServletRequest. args[0]={}", args[0]);
            }
            return false;
        }

        if (!(args[1] instanceof HttpServletResponse)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object, Not implemented of javax.servlet.http.HttpServletResponse. args[1]={}.", args[1]);
            }
            return false;
        }
        return true;
    }

    private int getStatusCode(final HttpServletResponse response) {
        try {
            return response.getStatus();
        } catch (Exception ignored) {
        }
        return 0;
    }
}