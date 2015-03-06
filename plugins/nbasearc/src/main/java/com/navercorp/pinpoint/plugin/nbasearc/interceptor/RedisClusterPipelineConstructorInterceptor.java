package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * RedisCluster pipeline(nBase-ARC client) constructor interceptor 
 * - trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineConstructorInterceptor extends GatewayServerMetadataReadInterceptor {

    public RedisClusterPipelineConstructorInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, MetadataAccessor destinationIdAccessor, MetadataAccessor endPointAccessor) {
        super(traceContext, methodDescriptor, destinationIdAccessor, endPointAccessor);
    }
}
