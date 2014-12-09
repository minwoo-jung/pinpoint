package com.navercorp.pinpoint.profiler.modifier.connector.nimm.interceptor;

import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.MetaObject;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * target lib = com.nhncorp.lucy.lucy-nimmconnector-2.1.4
 * 
 * @author netspider
 */
public class InvokeMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	// TODO nimm socket도 수집해야하나?? nimmAddress는 constructor에서 string으로 변환한 값을 들고
	// 있음.
	private MetaObject<String> getNimmAddress = new MetaObject<String>("__getNimmAddress");

	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			return;
		}

		// final long timeoutMillis = (Long) args[0];
		final String objectName = (String) args[1];
		final String methodName = (String) args[2];
		final Object[] params = (Object[]) args[3];

		// UUID format을 그대로.
		final boolean sampling = trace.canSampled();
		if (!sampling) {
			// TODO header 추가.
			return;
		}

		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceId nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		trace.recordServiceType(ServiceType.NIMM_CLIENT);

		// TODO protocol은 어떻게 표기하지???

		String nimmAddress = getNimmAddress.invoke(target);
		trace.recordDestinationId(nimmAddress);

		// DestinationId와 동일하므로 없는게 맞음.
		// trace.recordEndPoint(nimmAddress);

		if (objectName != null) {
			trace.recordAttribute(AnnotationKey.NIMM_OBJECT_NAME, objectName);
		}
		if (methodName != null) {
			trace.recordAttribute(AnnotationKey.NIMM_METHOD_NAME, methodName);
		}
		if (params != null) {
			trace.recordAttribute(AnnotationKey.NIMM_PARAM, Arrays.toString(params));
		}
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
			trace.recordApi(descriptor);
			trace.recordException(throwable);
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