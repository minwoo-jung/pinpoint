package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectorConstructorInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final MetadataAccessor serverAddressAccessor;
    
    public ConnectorConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(METADATA_NPC_SERVER_ADDRESS) MetadataAccessor serverAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.serverAddressAccessor = serverAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        InetSocketAddress serverAddress = null;

        if (target instanceof KeepAliveNpcHessianConnector) {
            /*
             * com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector.
             * KeepAliveNpcHessianConnector(InetSocketAddress, long, long,
             * Charset)
             */
            if (args.length == 4) {
                if (args[0] instanceof InetSocketAddress) {
                    serverAddress = (InetSocketAddress) args[0];
                }
            } else if (args.length == 1) {
                if (args[0] instanceof NpcConnectorOption) {
                    NpcConnectorOption option = (NpcConnectorOption) args[0];
                    serverAddress = option.getAddress();
                }
            }
        } else {
            if (args[0] instanceof NpcConnectorOption) {
                NpcConnectorOption option = (NpcConnectorOption) args[0];
                serverAddress = option.getAddress();
            }
        }

        serverAddressAccessor.set(target, serverAddress);

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        CallStackFrame recorder = trace.traceBlockBegin();
        recorder.markBeforeTime();
        recorder.recordServiceType(NPC_CLIENT_INTERNAL);

        if (serverAddress != null) {
            int port = serverAddress.getPort();
            String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
            recorder.recordAttribute(NPC_URL, endPoint);
        } else {
            // destination id가 없으면 안되기 때문에 unknown으로 지정.
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
            CallStackFrame recorder = trace.currentCallStackFrame();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            recorder.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}