package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.NimmAddressAccessor;

import java.util.Arrays;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 */
public class InvokeMethodInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    // TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고 있음.

    public InvokeMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
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

        // final long timeoutMillis = (Long) args[0];
        final String objectName = (String) args[1];
        final String methodName = (String) args[2];
        final Object[] params = (Object[]) args[3];

        // UUID format을 그대로.
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            // TODO header 추가.
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        recorder.recordServiceType(NIMM_CLIENT);

        // TODO protocol은 어떻게 표기하지???

        String nimmAddress = "";
        if (target instanceof NimmAddressAccessor) {
            nimmAddress = ((NimmAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }

        recorder.recordDestinationId(nimmAddress);

        // DestinationId와 동일하므로 없는게 맞음.
        // trace.recordEndPoint(nimmAddress);

        if (objectName != null) {
            recorder.recordAttribute(NIMM_OBJECT_NAME, objectName);
        }
        if (methodName != null) {
            recorder.recordAttribute(NIMM_METHOD_NAME, methodName);
        }
        if (params != null) {
            recorder.recordAttribute(NIMM_PARAM, Arrays.toString(params));
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
        if(throwable != null || result == null) {
            return false;
        }

        if (!(result instanceof AsyncTraceIdAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncTraceIdAccessor.class.getName());
            return false;
        }

        return true;
    }
}