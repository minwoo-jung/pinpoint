package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

public class CreateConnectorInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

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

        CallStackFrame recorder = trace.pushCallStackFrame();
        recorder.markBeforeTime();

        recorder.recordServiceType(NPC_CLIENT_INTERNAL);

        NpcConnectorOption option = (NpcConnectorOption) args[0];

        InetSocketAddress serverAddress = option.getAddress();

        if (serverAddress != null) {
            int port = serverAddress.getPort();
            String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
            recorder.recordAttribute(NPC_URL, endPoint);
        } else {
            recorder.recordAttribute(NPC_URL, "unknown");
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
            CallStackFrame recorder = trace.peekCallStackFrame();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            recorder.markAfterTime();
        } finally {
            trace.popCallStackFrame();
        }
    }
}