package com.navercorp.pinpoint.plugin.lucy.net.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author jaehong.kim
 */
public class DefaultInvocationFutureMethodInterceptor extends SpanAsyncEventSimpleAroundInterceptor implements LucyNetConstants {

    public DefaultInvocationFutureMethodInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        super(traceContext, methodDescriptor, asyncTraceIdAccessor);
    }

    @Override
    protected void doInBeforeTrace(Trace trace, AsyncTraceId asyncTraceId, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(ServiceType.INTERNAL_METHOD);
        trace.recordException(throwable);
        trace.recordApi(methodDescriptor);
        trace.markAfterTime();
    }
}
