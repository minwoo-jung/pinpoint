package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

public class InitializeConnectorInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final MetadataAccessor serverAddressAccessor;

    public InitializeConnectorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(METADATA_NPC_SERVER_ADDRESS) MetadataAccessor serverAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.serverAddressAccessor = serverAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // Trace trace = traceContext.currentRawTraceObject();
        // sampling 레이트를 추가로 확인하여 액션을 취하는 로직이 없으므로 그냥 currentTraceObject()fmf g
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(NPC_CLIENT_INTERNAL);

        InetSocketAddress serverAddress = serverAddressAccessor.get(target);
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

        Trace trace = traceContext.currentTraceObject();
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