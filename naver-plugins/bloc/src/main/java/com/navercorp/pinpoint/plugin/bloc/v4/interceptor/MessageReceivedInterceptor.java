/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapperAdaptor;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.BlocPluginConfig;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.navercorp.pinpoint.plugin.bloc.NpcRequest;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.NpcMessage;
import external.org.apache.mina.common.IoSession;


public class MessageReceivedInterceptor extends AbstractBlocAroundInterceptor {


    private final boolean traceRequestParam;

    private final ServerRequestRecorder<ServerRequestWrapper> serverRequestRecorder;
    private final RequestTraceReader<ServerRequestWrapper> requestTraceReader;

    public MessageReceivedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, MessageReceivedInterceptor.class);


        BlocPluginConfig config = new BlocPluginConfig(traceContext.getProfilerConfig());
        traceRequestParam = config.isBlocTraceRequestParam();
        RequestAdaptor<ServerRequestWrapper> requestAdaptor = new ServerRequestWrapperAdaptor();
        this.serverRequestRecorder = new ServerRequestRecorder<ServerRequestWrapper>(requestAdaptor);
        this.requestTraceReader = new RequestTraceReader<ServerRequestWrapper>(traceContext, requestAdaptor);
    }

    @Override
    protected boolean validateArgument(Object[] args) {
        if (args == null || args.length < 3) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[1] instanceof external.org.apache.mina.common.IoSession)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], external.org.apache.mina.common.IoSession.class.getName());
            }
            return false;
        }

        if (!(args[2] instanceof NpcMessage)) {
            if (isDebug) {
                logger.debug("Invalid args[2]={}. Need {}", args[2], NpcMessage.class.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        IoSession ioSession = (IoSession) args[1];
        NpcMessage npcMessage = (NpcMessage) args[2];

        final ServerRequestWrapper serverRequestWrapper = new NpcRequest(ioSession, npcMessage);
        final Trace trace = this.requestTraceReader.read(serverRequestWrapper);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            this.serverRequestRecorder.record(spanRecorder, serverRequestWrapper);
        }
        return trace;
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
        SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
        spanEventRecorder.recordApi(methodDescriptor);
        spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
        getParams(args, spanEventRecorder);
    }



    private void getParams(Object[] args, SpanEventRecorder spanEventRecorder) {
        if (traceRequestParam) {
            NpcMessage npcMessage = (NpcMessage) args[2];
            Object call = npcMessage.getPayload();

            if (call instanceof Call) {
                final String parameters = LucyNetUtils.getParameterAsString(((Call) call).getParameters(), MAX_EACH_PARAMETER_SIZE, MAX_ALL_PARAMETER_SIZE);
                spanEventRecorder.recordAttribute(BlocConstants.CALL_PARAM, parameters);
            }
        }
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        SpanEventRecorder spanEventRecorder = null;
        try {
            spanEventRecorder = trace.currentSpanEventRecorder();
        } finally {
            if (spanEventRecorder != null) {
                spanEventRecorder.recordException(throwable);
            }
            trace.traceBlockEnd();
        }
    }
}