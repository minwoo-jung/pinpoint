package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * Gateway(nBase-ARC client) getServer() method interceptor 
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayGetServerMethodInterceptor extends GatewayServerMetadataAttachInterceptor {

    public GatewayGetServerMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, MetadataAccessor destinationIdAccessor) {
        super(traceContext, methodDescriptor, destinationIdAccessor);
    }
}