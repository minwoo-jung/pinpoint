package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;

import java.net.InetSocketAddress;

public class InvokeInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public InvokeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
//        TraceId nextId = trace.getTraceId().getNextTraceId();
//        trace.recordNextSpanId(nextId.getSpanId());

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

        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT);

        if (target instanceof NpcServerAddressAccessor) {
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
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        if (isAsynchronousInvocation(target, args, result, throwable)) {
            // set asynchronous trace
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();

            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
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