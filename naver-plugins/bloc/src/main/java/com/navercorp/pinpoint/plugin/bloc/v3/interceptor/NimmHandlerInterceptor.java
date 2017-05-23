/*
 *  Copyright 2015 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.LucyNetHeader;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.npc.NpcMessage;

import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class NimmHandlerInterceptor extends AbstractBlocAroundInterceptor {

    public NimmHandlerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, NimmHandlerInterceptor.class);
    }

    @Override
    protected boolean validateArgument(Object[] args) {
        if (args == null || args.length != 2) {
            if (isDebug) {
                logger.debug("Invalid args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof NpcMessage)) {
            if (isDebug) {
                logger.debug("Invalid args[0]={}. Need {}", args[0], NpcMessage.class.getName());
            }
            return false;
        }

        if (!(args[1] instanceof NimmAddress)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], NimmAddress.class.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        NpcMessage npcMessage = (NpcMessage) args[0];

        Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

        String srcAddress = LucyNetUtils.nimmAddressToString((NimmAddress) args[1]);

        boolean sampling = sampleEnable(pinpointOptions);
        if (!sampling) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace remoteAddr:{}", srcAddress);
            }
            return trace;
        }

        String dstAddress = BlocConstants.UNKOWN_ADDRESS;
        if (target instanceof NimmServerSocketAddressAccessor) {
            dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }

        final String rpcName = LucyNetUtils.getRpcName(npcMessage);
        final TraceId traceId = populateTraceIdFromRequest(pinpointOptions);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, srcAddr:{}, dstAddr:{}, rpcName:{}", traceId, srcAddress, dstAddress, rpcName);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, srcAddr:{}, dstAddr:{}, rpcName:{}", traceId, srcAddress, dstAddress, rpcName);
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. srcAddr:{}, dstAddr:{}, rpcName:{}", srcAddress, dstAddress, rpcName);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. srcAddr:{}, dstAddr:{}, rpcName:{}", srcAddress, dstAddress, rpcName);
                }
            }
            return trace;
        }
    }

    private boolean sampleEnable(Map<String, String> pinpointOptions) {
        String samplingFlag = pinpointOptions.get(LucyNetHeader.PINPOINT_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(Map<String, String> pinpointOptions) {
        String transactionId = pinpointOptions.get(LucyNetHeader.PINPOINT_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanID = NumberUtils.parseLong(pinpointOptions.get(LucyNetHeader.PINPOINT_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(pinpointOptions.get(LucyNetHeader.PINPOINT_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(pinpointOptions.get(LucyNetHeader.PINPOINT_FLAGS.toString()), (short) 0);

            final TraceId id = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
        SpanRecorder spanRecorder = trace.getSpanRecorder();

        try {
            spanRecorder.recordServiceType(BlocConstants.BLOC);

            NpcMessage npcMessage = (NpcMessage) args[0];
            spanRecorder.recordRpcName(LucyNetUtils.getRpcName(npcMessage));

            String srcAddress = LucyNetUtils.nimmAddressToString((NimmAddress) args[1]);
            spanRecorder.recordRemoteAddress(srcAddress);

            String dstAddress = BlocConstants.UNKOWN_ADDRESS;
            if (target instanceof NimmServerSocketAddressAccessor) {
                dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
            }
            spanRecorder.recordEndPoint(dstAddress);

            if (!spanRecorder.isRoot()) {
                Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

                final String parentApplicationName = pinpointOptions.get(LucyNetHeader.PINPOINT_PARENT_APPLICATION_NAME.toString());
                if (parentApplicationName != null) {
                    final String host = pinpointOptions.get(LucyNetHeader.PINPOINT_HOST.toString());
                    if (host != null) {
                        spanRecorder.recordAcceptorHost(host);
                    } else {
                        spanRecorder.recordAcceptorHost(dstAddress);
                    }

                    final String type = pinpointOptions.get(LucyNetHeader.PINPOINT_PARENT_APPLICATION_TYPE.toString());
                    final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
                    spanRecorder.recordParentApplication(parentApplicationName, parentApplicationType);
                }
            }

            spanRecorder.recordApi(blocMethodApiTag);
        } finally {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordApi(methodDescriptor);
            spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
        }
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        SpanEventRecorder spanEventRecorder = null;
        try {
            spanEventRecorder = trace.currentSpanEventRecorder();
            if (traceRequestParam) {
                NpcMessage npcMessage = (NpcMessage) args[0];
                Object call = npcMessage.getPayload();
                if (call instanceof Call) {
                    final String parameters = LucyNetUtils.getParameterAsString(((Call) call).getParameters(), MAX_EACH_PARAMETER_SIZE, MAX_ALL_PARAMETER_SIZE);
                    spanEventRecorder.recordAttribute(BlocConstants.CALL_PARAM, parameters);
                }
            }
        } finally {
            if (spanEventRecorder != null) {
                spanEventRecorder.recordException(throwable);
            }
            trace.traceBlockEnd();
        }
    }

}