package com.nhn.pinpoint.profiler.modifier.connector.npc.interceptor;

import java.net.InetSocketAddress;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceId;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;

/**
 * based on NPC client 1.5.18
 * 
 * @author netspider
 * 
 */
public class ConnectInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	// private int apiId;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			logger.warn("Trace object is null");
			return;
		}

		com.nhncorp.lucy.npc.connector.NpcConnectorOption connectorOption = (com.nhncorp.lucy.npc.connector.NpcConnectorOption) args[0];

		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		trace.recordServiceType(ServiceType.NPC_CLIENT);

		InetSocketAddress serverAddress = connectorOption.getAddress();
		int port = serverAddress.getPort();
        String endpiont = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endpiont);
		trace.recordDestinationId(endpiont);

		trace.recordAttribute(AnnotationKey.NPC_URL, serverAddress.toString());
		trace.recordAttribute(AnnotationKey.NPC_CONNECT_OPTION, connectorOption.toString());
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			// result는 로깅하지 않는다.
			logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

        try {
            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}
}