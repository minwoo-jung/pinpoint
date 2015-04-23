package com.navercorp.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.ParameterUtils;

/**
 * @author emeroad
 */
@Group(ArcusConstants.ARCUS_SCOPE)
public class ApiInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements ArcusConstants {
    private final boolean traceKey;
    private final int keyIndex;
    
    private final MetadataAccessor serviceCodeAccessor;
    private final MetadataAccessor operationAccessor;
    
    public ApiInterceptor(TraceContext context, MethodInfo targetMethod,
            @Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor, @Name(METADATA_OPERATION) MetadataAccessor operationAccessor, boolean traceKey) {
        super(context, targetMethod.getDescriptor());
        
        if (traceKey) {
            int index = ParameterUtils.findFirstString(targetMethod, 3);
        
            if (index != -1) {
                this.traceKey = true;
                this.keyIndex = index; 
            } else {
                this.traceKey = false;
                this.keyIndex = -1;
            }
        } else {
            this.traceKey = false;
            this.keyIndex = -1;
        }
        
        this.serviceCodeAccessor = serviceCodeAccessor;
        this.operationAccessor = operationAccessor;
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        if (traceKey) {
            final Object recordObject = args[keyIndex];
            trace.recordApi(getMethodDescriptor(), recordObject, keyIndex);
        } else {
            trace.recordApi(getMethodDescriptor());
        }

        // find the target node
        if (result instanceof Future && operationAccessor.isApplicable(result)) {
            Operation op = operationAccessor.get(result);
            
            if (op != null) {
                MemcachedNode handlingNode = op.getHandlingNode();
                SocketAddress socketAddress = handlingNode.getSocketAddress();
     
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    trace.recordEndPoint(address.getHostName() + ":" + address.getPort());
                }
            } else {
                logger.info("operation not found");
            }
        }

        // determine the service type
        String serviceCode = serviceCodeAccessor.get(target);
        
        if (serviceCode != null) {
            trace.recordDestinationId(serviceCode);
            trace.recordServiceType(ARCUS);
        } else {
            trace.recordDestinationId("MEMCACHED");
            trace.recordServiceType(ServiceType.MEMCACHED);
        }

        trace.markAfterTime();
    }
}
