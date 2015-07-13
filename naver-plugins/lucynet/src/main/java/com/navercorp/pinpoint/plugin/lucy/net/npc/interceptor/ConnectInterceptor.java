package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public ConnectInterceptor(MethodDescriptor descriptor, TraceContext traceContext) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // Trace trace = traceContext.currentRawTraceObject();
        // sampling 레이트를 추가로 확인하여 액션을 취하는 로직이 없으므로 그냥 currentTraceObject()를 호출한다.
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        com.nhncorp.lucy.npc.connector.NpcConnectorOption connectorOption = (com.nhncorp.lucy.npc.connector.NpcConnectorOption) args[0];

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.markBeforeTime();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        recorder.recordServiceType(NPC_CLIENT);

        InetSocketAddress serverAddress = connectorOption.getAddress();
        int port = serverAddress.getPort();
        String endpiont = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endpiont);
        recorder.recordDestinationId(endpiont);

        recorder.recordAttribute(NPC_URL, serverAddress.toString());
        recorder.recordAttribute(NPC_CONNECT_OPTION, connectorOption.toString());
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

            recorder.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}