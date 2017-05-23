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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * RedisCluster(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public RedisClusterMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = null;
        String endPoint = null;

        if ((target instanceof DestinationIdAccessor) && (target instanceof EndPointAccessor)) {
            destinationId = ((DestinationIdAccessor)target)._$PINPOINT$_getDestinationId();
            endPoint = ((EndPointAccessor)target)._$PINPOINT$_getEndPoint();
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(destinationId != null ? destinationId : NbaseArcConstants.NBASE_ARC.toString());
        recorder.recordServiceType(NbaseArcConstants.NBASE_ARC);
        recorder.recordException(throwable);
    }
}