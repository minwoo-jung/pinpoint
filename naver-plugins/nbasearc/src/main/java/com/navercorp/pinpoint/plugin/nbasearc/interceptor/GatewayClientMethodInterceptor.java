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
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.nbasearc.CommandContext;
import com.navercorp.pinpoint.plugin.nbasearc.CommandContextFactory;
import com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;

/**
 * RedisCluster(nBase-ARC client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
@Group(NbaseArcConstants.NBASE_ARC_SCOPE)
public class GatewayClientMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private InterceptorGroup interceptorGroup;
    private boolean io;

    public GatewayClientMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup, boolean io) {
        super(traceContext, methodDescriptor);

        this.interceptorGroup = interceptorGroup;
        this.io = io;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        final InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
        if (invocation != null) {
            invocation.getOrCreateAttachment(CommandContextFactory.COMMAND_CONTEXT_FACTORY);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String destinationId = null;
        String endPoint = null;

        if (target instanceof DestinationIdAccessor) {
            destinationId = ((DestinationIdAccessor) target)._$PINPOINT$_getDestinationId();
        }

        final InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
        if (invocation != null && invocation.getAttachment() != null) {
            final CommandContext commandContext = (CommandContext) invocation.getAttachment();
            endPoint = commandContext.getEndPoint();
            logger.debug("Check command context {}", commandContext);
            if (io) {
                final StringBuilder sb = new StringBuilder();
                sb.append("write=").append(commandContext.getWriteElapsedTime());
                if (commandContext.isWriteFail()) {
                    sb.append("(fail)");
                }
                sb.append(", read=").append(commandContext.getReadElapsedTime());
                if (commandContext.isReadFail()) {
                    sb.append("(fail)");
                }
                recorder.recordAttribute(AnnotationKey.ARGS0, sb.toString());
            }
            // clear
            invocation.removeAttachment();
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(destinationId != null ? destinationId : NbaseArcConstants.NBASE_ARC.toString());
        recorder.recordServiceType(NbaseArcConstants.NBASE_ARC);
        recorder.recordException(throwable);
    }
}