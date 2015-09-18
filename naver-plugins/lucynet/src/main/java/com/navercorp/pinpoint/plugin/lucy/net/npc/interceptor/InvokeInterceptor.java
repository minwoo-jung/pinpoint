package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.NpcServerAddressAccessor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class InvokeInterceptor implements AroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public InvokeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

//        Trace trace = traceContext.currentRawTraceObject();
        // 이부분은 현재 remote 호출시 traceId를 넣지 않으므로 currentRawTraceObject()를 호출하지 않는다.
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        String objectName;
        String methodName;
        Charset charset;
        Object params;

        if (args.length == 3) {
            objectName = (String) args[0];
            methodName = (String) args[1];
            // TODO charset을 com.nhncorp.lucy.npc.connector.AbstractConnector.getDefaultCharset() 에서 조회 가능하긴 함.
            charset = null;
            params = args[2];
        } else if (args.length == 4) {
            objectName = (String) args[0];
            methodName = (String) args[1];
            charset = (Charset) args[2];
            params = args[3];
        }

        //
        // TODO add sampling logic here.
        //

        SpanEventRecorder recorder = trace.traceBlockBegin();
//        TraceId nextId = trace.getTraceId().getNextTraceId();
//        trace.recordNextSpanId(nextId.getSpanId());

        //
        // TODO add pinpoint headers to the request message here.
        //

        recorder.recordServiceType(NPC_CLIENT);

        InetSocketAddress serverAddress = null;
        if (target instanceof NpcServerAddressAccessor) {
            serverAddress = ((NpcServerAddressAccessor) target)._$PINPOINT$_getNpcServerAddress();
        }

        int port = serverAddress.getPort();
        String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endPoint);
        recorder.recordDestinationId(endPoint);

        recorder.recordAttribute(NPC_URL, serverAddress.toString());
    }

    @Override
    public void after(Object target, Object result, Throwable throwable, Object[] args) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
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