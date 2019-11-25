package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

import com.nhncorp.lucy.bloc.core.processor.BlocRequest;

public class ProcessInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        if (args != null && args.length >= 1 && args[0] instanceof BlocRequest) {
            BlocRequest blocRequest = (BlocRequest)args[0];
            recorder.recordAttribute(BlocConstants.CALL_URL, blocRequest.getPath());
            recorder.recordAttribute(BlocConstants.PROTOCOL, blocRequest.getProtocol());
        }
    }

}

