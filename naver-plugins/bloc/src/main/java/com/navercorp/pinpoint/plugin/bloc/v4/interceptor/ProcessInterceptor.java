package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.nhncorp.lucy.bloc.core.processor.BlocRequest;

@TargetMethod(name="process", paramTypes="com.nhncorp.lucy.bloc.core.processor.BlocRequest")
public class ProcessInterceptor implements SimpleAroundInterceptor, BlocConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public ProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        
        if (trace == null) {
            return;
        }

        CallStackFrame recorder = trace.traceBlockBegin();
        recorder.markBeforeTime();

        recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        
        if (trace == null) {
            return;
        }

        try {
            CallStackFrame recorder = trace.currentCallStackFrame();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            
            if (args[0] != null) {
                BlocRequest blocRequest = (BlocRequest)args[0];
                recorder.recordAttribute(CALL_URL, blocRequest.getPath());
                recorder.recordAttribute(PROTOCOL, blocRequest.getProtocol());
            }

            recorder.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}

