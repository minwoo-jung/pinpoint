/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.owfs.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.owfs.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.owfs.EndPointAccessor;
import com.navercorp.pinpoint.plugin.owfs.OwfsPluginConstants;

/**
 * @author jaehong.kim
 */
public class OwfsFileInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public OwfsFileInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(getMethodDescriptor());
        recorder.recordException(throwable);
        recorder.recordServiceType(OwfsPluginConstants.OWFS);
        recorder.recordDestinationId("owfs");
        recorder.recordEndPoint("unknown");
        if (target instanceof DestinationIdAccessor) {
            final String destinationId = ((DestinationIdAccessor) target)._$PINPOINT$_getDestinationId();
            if (destinationId != null) {
                recorder.recordDestinationId(destinationId);
                if (isDebug) {
                    logger.debug("Record destinationId={}", destinationId);
                }
            }
        }

        if (target instanceof EndPointAccessor) {
            final String endPoint = ((EndPointAccessor) target)._$PINPOINT$_getEndPoint();
            if (endPoint != null) {
                recorder.recordEndPoint(endPoint);
                if (isDebug) {
                    logger.debug("Record endPoint={}", endPoint);
                }
            }
        }
    }
}