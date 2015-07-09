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
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * RedisCluster pipeline(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
@Group(NbaseArcConstants.NBASE_ARC_SCOPE)
public class RedisClusterPipelineMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements NbaseArcConstants {

    private MetadataAccessor destinationIdAccessor;
    private MetadataAccessor endPointAccessor;

    public RedisClusterPipelineMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        super(traceContext, methodDescriptor);

        this.destinationIdAccessor = destinationIdAccessor;
        this.endPointAccessor = endPointAccessor;
    }

    @Override
    public void doInBeforeTrace(CallStackFrame recorder, Object target, Object[] args) {
        recorder.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(CallStackFrame recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = null;
        String endPoint = null;

        if (destinationIdAccessor.isApplicable(target) && endPointAccessor.isApplicable(target)) {
            destinationId = destinationIdAccessor.get(target);
            endPoint = endPointAccessor.get(target);
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(destinationId != null ? destinationId : NBASE_ARC.toString());
        recorder.recordServiceType(NBASE_ARC);
        recorder.recordException(throwable);
        recorder.markAfterTime();
    }
}