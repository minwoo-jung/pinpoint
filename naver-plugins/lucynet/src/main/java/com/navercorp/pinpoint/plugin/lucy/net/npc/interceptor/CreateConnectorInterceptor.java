package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

import java.net.InetSocketAddress;

public class CreateConnectorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public CreateConnectorInterceptor(MethodDescriptor descriptor, TraceContext traceContext) {
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
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT_INTERNAL);

        InetSocketAddress serverAddress = null;
        if (args != null && args.length >= 1 && args[0] instanceof NpcConnectorOption) {
            NpcConnectorOption option = (NpcConnectorOption) args[0];
            serverAddress = option.getAddress();
        }

        final String endPoint = getEndPoint(serverAddress);
        recorder.recordAttribute(LucyNetConstants.NPC_URL, endPoint);
    }

    private String getEndPoint(InetSocketAddress serverAddress) {
        if (serverAddress != null) {
            return EndPointUtils.getEndPoint(serverAddress);
        } else {
            return "unknown";
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
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
        } finally {
            trace.traceBlockEnd();
        }
    }
}