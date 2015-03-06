package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * GatewayServer(nBase-ARC client) getResource() method interceptor 
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayServerGetResourceMethodInterceptor extends GatewayServerMetadataAttachInterceptor {

    public GatewayServerGetResourceMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, MetadataAccessor destinationIdAccessor) {
        super(traceContext, methodDescriptor, destinationIdAccessor);
    }
}