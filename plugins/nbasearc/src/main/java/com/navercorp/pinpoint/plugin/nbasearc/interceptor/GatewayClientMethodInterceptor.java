/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupTransaction;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * RedisCluster(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
@Group(NbaseArcConstants.NBASE_ARC_SCOPE)
public class GatewayClientMethodInterceptor extends SpanEventSimpleAroundInterceptor implements NbaseArcConstants {

    private MetadataAccessor destinationIdAccessor;
    private InterceptorGroup interceptorGroup;

    public GatewayClientMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor, InterceptorGroup interceptorGroup) {
        super(GatewayClientMethodInterceptor.class);

        this.destinationIdAccessor = destinationIdAccessor;
        this.interceptorGroup = interceptorGroup;

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

        if (destinationIdAccessor.isApplicable(target)) {
            destinationId = destinationIdAccessor.get(target);
        }
        
        InterceptorGroupTransaction scope = interceptorGroup.getCurrentTransaction();
        if(scope != null) {
            endPoint = (String) scope.getAttachment();
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        trace.recordDestinationId(destinationId != null ? destinationId : NBASE_ARC.toString());
        trace.recordServiceType(NBASE_ARC);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}