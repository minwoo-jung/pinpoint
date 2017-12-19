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
import com.navercorp.pinpoint.plugin.owfs.EndPointUtils;
import com.navercorp.pinpoint.plugin.owfs.OwfsPluginConstants;

import java.net.InetAddress;

/**
 * @author jaehong.kim
 */
public class OwfsInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public OwfsInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(getMethodDescriptor());
        recorder.recordServiceType(OwfsPluginConstants.OWFS);
        recorder.recordException(throwable);
        recorder.recordDestinationId("owfs");
        recorder.recordEndPoint("unknown");
        if (validate(args)) {
            final String endPoint = EndPointUtils.getEndPoint(args[0].toString());
            if(endPoint != null) {
                recorder.recordEndPoint(endPoint);
            }
            final String destinationId = (String) args[1];
            if(destinationId != null) {
                recorder.recordDestinationId(destinationId);
            }
            if (isDebug) {
                logger.debug("Record destinationId={}, endPoint={}", destinationId, endPoint);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 2) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }
        // java.net.InetAddress
        if (!(args[0] instanceof InetAddress)) {
            if (isDebug) {
                logger.debug("Invalid arguments. Expect InetAddress but args[0]({}).", args[0]);
            }
            return false;
        }
        // java.lang.String
        if (!(args[1] instanceof String)) {
            if (isDebug) {
                logger.debug("Invalid arguments. Expect String but args[1]({}).", args[1]);
            }
            return false;
        }
        return true;
    }
}
