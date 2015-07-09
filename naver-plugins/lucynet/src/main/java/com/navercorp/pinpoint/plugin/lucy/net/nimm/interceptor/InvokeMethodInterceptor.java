package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 */
@TargetMethod(name = "invoke", paramTypes = { "long", "java.lang.String", "java.lang.String", "java.lang.Object[]" })
public class InvokeMethodInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final MetadataAccessor nimmAddressAccessor;
    private final MetadataAccessor asyncTraceIdAccessor;

    // TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고 있음.

    public InvokeMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, @Name(METADATA_NIMM_ADDRESS) MetadataAccessor nimmAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
        this.nimmAddressAccessor = nimmAddressAccessor;
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

        CallStackFrame recorder = trace.pushCallStackFrame();
        recorder.markBeforeTime();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        recorder.recordServiceType(NIMM_CLIENT);

        // TODO protocol은 어떻게 표기하지???

        String nimmAddress = nimmAddressAccessor.get(target);
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
            CallStackFrame recorder = trace.peekCallStackFrame();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            recorder.markAfterTime();

            if (isAsynchronousInvocation(target, args, result, throwable)) {
                // set asynchronous trace
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                asyncTraceIdAccessor.set(result, asyncTraceId);
                if (isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
                }
            }

        } finally {
            trace.popCallStackFrame();
        }
    }
    
    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if(throwable != null || result == null) {
            return false;
        }

        if (!asyncTraceIdAccessor.isApplicable(result)) {
            logger.debug("Invalid result object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }
}