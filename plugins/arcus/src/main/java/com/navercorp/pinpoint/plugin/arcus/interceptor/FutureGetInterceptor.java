package com.navercorp.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.CacheApi;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusMetadata;
import com.navercorp.pinpoint.plugin.arcus.ArcusServiceTypes;

/**
 * @author emeroad
 */
public class FutureGetInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    
    public FutureGetInterceptor(@CacheApi MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.recordApi(methodDescriptor);
//            중요한 파라미터가 아님 레코딩 안함.
//            String annotation = "future.get() timeout:" + args[0] + " " + ((TimeUnit)args[1]).name();
//            trace.recordAttribute(AnnotationKey.ARCUS_COMMAND, annotation);

            // find the target node
            final Operation op = ArcusMetadata.OPERATION.get(target);
            
            if (op != null) {
                MemcachedNode handlingNode = op.getHandlingNode();
                if (handlingNode != null) {
                    SocketAddress socketAddress = handlingNode.getSocketAddress();
                    if (socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress address = (InetSocketAddress) socketAddress;
                        trace.recordEndPoint(address.getHostName() + ":" + address.getPort());
                    }
                } else {
                    logger.info("no handling node");
                }
            } else {
                logger.info("operation not found");
            }

            // determine the service type
            String serviceCode = ArcusMetadata.SERVICE_CODE.get(op);
            if (serviceCode != null) {
                trace.recordDestinationId(serviceCode);
                trace.recordServiceType(ArcusServiceTypes.ARCUS_FUTURE_GET);
            } else {
                trace.recordDestinationId("MEMCACHED");
                trace.recordServiceType(ServiceType.MEMCACHED_FUTURE_GET);
            }

            if (op != null) {
                trace.recordException(op.getException());
            }
//            cancel일때 exception은 안던지는 것인가?
//            if (op.isCancelled()) {
//                trace.recordAttribute(AnnotationKey.EXCEPTION, "cancelled by user");
//            }

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}
