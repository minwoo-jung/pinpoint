package com.navercorp.pinpoint.plugin.lucy.net.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author jaehong.kim
 */
public class DefaultInvocationFutureMethodInterceptor extends SpanAsyncEventSimpleAroundInterceptor {

    public DefaultInvocationFutureMethodInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
    }
}
