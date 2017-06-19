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
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;
import com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector;
import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

import java.net.InetSocketAddress;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectorConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public ConnectorConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
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
            if (args != null && args.length == 4) {
                if (args[0] instanceof InetSocketAddress) {
                    serverAddress = (InetSocketAddress) args[0];
                }
            } else if (args != null && args.length == 1) {
                if (args[0] instanceof NpcConnectorOption) {
                    NpcConnectorOption option = (NpcConnectorOption) args[0];
                    serverAddress = option.getAddress();
                }
            }
        } else {
            if (args != null && args.length >= 1 && args[0] instanceof NpcConnectorOption) {
                NpcConnectorOption option = (NpcConnectorOption) args[0];
                serverAddress = option.getAddress();
            }
        }

        if (target instanceof NpcServerAddressAccessor) {
            ((NpcServerAddressAccessor) target)._$PINPOINT$_setNpcServerAddress(serverAddress);
        }

        Trace trace = traceContext.currentTraceObject();
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
        } else {
            // destination id가 없으면 안되기 때문에 unknown으로 지정.
            return LucyNetConstants.UNKOWN_ADDRESS;
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