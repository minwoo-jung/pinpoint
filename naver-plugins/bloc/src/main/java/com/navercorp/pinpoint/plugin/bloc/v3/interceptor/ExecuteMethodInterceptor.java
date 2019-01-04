/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.BlocPluginConfig;
import com.navercorp.pinpoint.plugin.bloc.HttpServerRequestAdaptor;
import external.org.apache.coyote.Request;

import java.util.Enumeration;

/**
 * @author netspider
 * @author emeroad
 */
public class ExecuteMethodInterceptor extends AbstractBlocAroundInterceptor {

    private final boolean traceRequestParam;
    private final ProxyRequestRecorder<Request> proxyRequestRecorder;
    private final ServerRequestRecorder<Request> serverRequestRecorder;
    private final RequestTraceReader<Request> requestTraceReader;

    public ExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<Request> requestRecorderFactory) {
        super(traceContext, descriptor, ExecuteMethodInterceptor.class);

        BlocPluginConfig config = new BlocPluginConfig(traceContext.getProfilerConfig());
        traceRequestParam = config.isBlocTraceRequestParam();
        RequestAdaptor<Request> requestAdaptor = new HttpServerRequestAdaptor();
        this.proxyRequestRecorder = requestRecorderFactory.getProxyRequestRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable(), requestAdaptor);
        this.serverRequestRecorder = new ServerRequestRecorder<Request>(requestAdaptor);
        this.requestTraceReader = new RequestTraceReader<Request>(traceContext, requestAdaptor);
    }

    @Override
    protected boolean validateArgument(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof Request)) {
            if (isDebug) {
                logger.debug("Invalid args[0]={}. Need {}", args[0], Request.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final Request request = (Request) args[0];
        final Trace trace = requestTraceReader.read(request);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            this.serverRequestRecorder.record(spanRecorder, request);
            // record proxy HTTP headers.
            this.proxyRequestRecorder.record(spanRecorder, request);
        }
        return trace;
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
        SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
        spanEventRecorder.recordApi(methodDescriptor);
        spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        SpanEventRecorder spanEventRecorder = null;
        try {
            spanEventRecorder = trace.currentSpanEventRecorder();
            if (traceRequestParam) {
                Request request = (Request) args[0];
                // skip
                String parameters = getRequestParameter(request, MAX_EACH_PARAMETER_SIZE, MAX_ALL_PARAMETER_SIZE);
                spanEventRecorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }
        } finally {
            if (spanEventRecorder != null) {
                spanEventRecorder.recordException(throwable);
            }
            trace.traceBlockEnd();
        }

    }

    private String getRequestParameter(Request request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameters().getParameterNames();
        final StringBuilder params = new StringBuilder(64);
        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            if (params.length() > totalLimit) {
                // 데이터 사이즈가 너무 클 경우 뒷 파라미터 생략.
                params.append("...");
                return params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = request.getParameters().getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
    }

}