package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author Taejin Koo
 */
public class OldVersionConnectorConstructorInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final MetadataAccessor serverAddressAccessor;

    public OldVersionConnectorConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor,
            @Name(METADATA_NPC_SERVER_ADDRESS) MetadataAccessor serverAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.serverAddressAccessor = serverAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        InetSocketAddress serverAddress = null;

        if (args.length > 0) {
            if (args[0] instanceof InetSocketAddress) {
                serverAddress = (InetSocketAddress) args[0];
            }
        }

        serverAddressAccessor.set(target, serverAddress);

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
        trace.recordServiceType(NPC_CLIENT_INTERNAL);

        if (serverAddress != null) {
            int port = serverAddress.getPort();
            String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
            trace.recordAttribute(NPC_URL, endPoint);
        } else {
            trace.recordAttribute(NPC_URL, "unknown");
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
            trace.recordApi(descriptor);
            trace.recordException(throwable);
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

}
