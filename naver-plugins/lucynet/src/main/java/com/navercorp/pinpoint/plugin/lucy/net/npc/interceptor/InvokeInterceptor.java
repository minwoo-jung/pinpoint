package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;

import java.net.InetSocketAddress;

public class InvokeInterceptor implements AroundInterceptor {

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

//        String objectName;
//        String methodName;
//        Charset charset;
//        Object params;

//        if (args != null && args.length == 3) {
//            objectName = (String) args[0];
//            methodName = (String) args[1];
//            // TODO charset을 com.nhncorp.lucy.npc.connector.AbstractConnector.getDefaultCharset() 에서 조회 가능하긴 함.
//            charset = null;
//            params = args[2];
//        } else if (args != null && args.length == 4) {
//            objectName = (String) args[0];
//            methodName = (String) args[1];
//            charset = (Charset) args[2];
//            params = args[3];
//        }

        //
        // TODO add sampling logic here.
        //

        SpanEventRecorder recorder = trace.traceBlockBegin();
//        TraceId nextId = trace.getTraceId().getNextTraceId();
//        trace.recordNextSpanId(nextId.getSpanId());

        //
        // TODO add pinpoint headers to the request message here.
        //

        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT);

        if (target != null && target instanceof NpcServerAddressAccessor) {
            InetSocketAddress serverAddress = ((NpcServerAddressAccessor) target)._$PINPOINT$_getNpcServerAddress();

            if (serverAddress != null) {
                final String endPoint = EndPointUtils.getEndPoint(serverAddress);

                //      DestinationId와 동일하므로 없는게 맞음.
                //        trace.recordEndPoint(endPoint);
                recorder.recordDestinationId(endPoint);
                recorder.recordAttribute(LucyNetConstants.NPC_URL, serverAddress.toString());
                return;
            }
        }

        recorder.recordDestinationId(LucyNetConstants.UNKOWN_ADDRESS);
        recorder.recordAttribute(LucyNetConstants.NPC_URL, LucyNetConstants.UNKOWN_ADDRESS);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
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
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();

                ((AsyncContextAccessor)result)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext {}", asyncContext);
                }
            }

            
        } finally {
            trace.traceBlockEnd();
        }
    }
    
    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncContextAccessor.class.getName());
            return false;
        }

        return true;
    }

}