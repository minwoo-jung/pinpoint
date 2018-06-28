package com.navercorp.pinpoint.plugin.bloc.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.HttpServerRequestWrapper;
import external.org.apache.coyote.Request;

import java.util.Enumeration;

/**
 * @author netspider
 * @author emeroad
 */
public class ExecuteMethodInterceptor extends AbstractBlocAroundInterceptor {

    public ExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ExecuteMethodInterceptor.class);
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
        final ServerRequestWrapper serverRequestWrapper = new HttpServerRequestWrapper(request);
        final Trace trace = requestTraceReader.read(serverRequestWrapper);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            this.serverRequestRecorder.record(spanRecorder, serverRequestWrapper);
            // record proxy HTTP headers.
            this.proxyHttpHeaderRecorder.record(spanRecorder, serverRequestWrapper);
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
            params.append("=");
            Object value = request.getParameters().getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
    }

}