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

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * GatewayClient(nBase-ARC client) internal method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class GatewayClientInternalMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements NbaseArcConstants {

    public GatewayClientInternalMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(CallStackFrame recorder, Object target, Object[] args) {
        recorder.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(CallStackFrame recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(getMethodDescriptor());
        recorder.recordServiceType(NBASE_ARC_INTERNAL);
        recorder.recordException(throwable);
        recorder.markAfterTime();
    }
}