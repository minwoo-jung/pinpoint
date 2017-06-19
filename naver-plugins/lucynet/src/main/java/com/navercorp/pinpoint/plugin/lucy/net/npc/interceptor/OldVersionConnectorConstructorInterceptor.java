package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class OldVersionConnectorConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public OldVersionConnectorConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        InetSocketAddress serverAddress = null;

        if (ArrayUtils.hasLength(args)) {
            if (args[0] instanceof InetSocketAddress) {
                serverAddress = (InetSocketAddress) args[0];
            }
        }

        if (target instanceof NpcServerAddressAccessor) {
            ((NpcServerAddressAccessor)target)._$PINPOINT$_setNpcServerAddress(serverAddress);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT_INTERNAL);

        final String endPoint = getEndPoint(serverAddress);
        recorder.recordAttribute(LucyNetConstants.NPC_URL, endPoint);
    }

    private String getEndPoint(InetSocketAddress serverAddress) {
        if (serverAddress != null) {
            return EndPointUtils.getEndPoint(serverAddress);
        }
        return "unknown";
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
