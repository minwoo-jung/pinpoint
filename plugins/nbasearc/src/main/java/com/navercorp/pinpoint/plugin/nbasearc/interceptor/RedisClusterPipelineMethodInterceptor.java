package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * RedisCluster pipeline(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineMethodInterceptor extends SpanEventSimpleAroundInterceptor implements NbaseArcConstants {

    private MetadataAccessor destinationIdAccessor;
    private MetadataAccessor endPointAccessor;

    public RedisClusterPipelineMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        super(RedisClusterPipelineMethodInterceptor.class);

        this.destinationIdAccessor = destinationIdAccessor;
        this.endPointAccessor = endPointAccessor;

        setTraceContext(traceContext);
        setMethodDescriptor(methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = null;
        String endPoint = null;

        if (destinationIdAccessor.isApplicable(target) && endPointAccessor.isApplicable(target)) {
            destinationId = destinationIdAccessor.get(target);
            endPoint = endPointAccessor.get(target);
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        trace.recordDestinationId(destinationId != null ? destinationId : NBASE_ARC.toString());
        trace.recordServiceType(NBASE_ARC);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}