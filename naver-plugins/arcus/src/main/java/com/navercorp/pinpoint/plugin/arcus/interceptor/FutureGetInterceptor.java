package com.navercorp.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Group(ArcusConstants.ARCUS_FUTURE_SCOPE)
public class FutureGetInterceptor extends SpanAsyncEventSimpleAroundInterceptor implements ArcusConstants {

    private final MetadataAccessor operationAccessor;
    private final MetadataAccessor serviceCodeAccessor;

    public FutureGetInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, @Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor,
            @Name(METADATA_OPERATION) MetadataAccessor operationAccessor) {
        super(traceContext, methodDescriptor, asyncTraceIdAccessor);

        this.serviceCodeAccessor = serviceCodeAccessor;
        this.operationAccessor = operationAccessor;
    }

    @Override
    protected void doInBeforeTrace(Trace trace, AsyncTraceId asyncTraceId, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        // find the target node
        final Operation op = operationAccessor.get(target);
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
        String serviceCode = serviceCodeAccessor.get(op);
        if (serviceCode != null) {
            trace.recordDestinationId(serviceCode);
            trace.recordServiceType(ARCUS_FUTURE_GET);
        } else {
            trace.recordDestinationId("MEMCACHED");
            trace.recordServiceType(ServiceType.MEMCACHED_FUTURE_GET);
        }

        if (op != null) {
            trace.recordException(op.getException());
        }
        trace.recordApi(methodDescriptor);
        trace.markAfterTime();
    }
}
