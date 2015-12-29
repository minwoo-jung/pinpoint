package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.LucyNetHeader;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.NpcMessage;

import java.util.Map;

/**
 * @Author Taejin Koo
 */
@TargetMethod(name = "handleResponseMessage", paramTypes = {"com.nhncorp.lucy.npc.NpcMessage", "java.lang.String"})
public class NimmHandlerInterceptor extends SpanSimpleAroundInterceptor {

    public NimmHandlerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, NimmHandlerInterceptor.class);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        if (!validate(args)) {
            return null;
        }

        NpcMessage npcMessage = (NpcMessage) args[0];
        String srcAddress = (String) args[1];

        String dstAddress = BlocConstants.UNKOWN_ADDRESS;
        if (target instanceof NimmServerSocketAddressAccessor) {
            dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }

        Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

        boolean sampling = sampleEnable(pinpointOptions);
        if (!sampling) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace remoteAddr:{}", srcAddress);
            }
            return trace;
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
                    logger.debug("TraceID not exist. start new trace. traceId:{}, srcAddr:{}, dstAddr:{}, rpcName:{}", traceId, srcAddress, dstAddress, rpcName);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. traceId:{}, srcAddr:{}, dstAddr:{}, rpcName:{}", traceId, srcAddress, dstAddress, rpcName);
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
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        NpcMessage npcMessage = (NpcMessage) args[0];
        String srcAddress = (String) args[1];

        String dstAddress = BlocConstants.UNKOWN_ADDRESS;
        if (target instanceof NimmServerSocketAddressAccessor) {
            dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }

        recorder.recordServiceType(BlocConstants.BLOC);
        recorder.recordRpcName(LucyNetUtils.getRpcName(npcMessage));
        recorder.recordEndPoint(dstAddress);
        recorder.recordRemoteAddress(srcAddress);

        if (!recorder.isRoot()) {
            Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

            final String parentApplicationName = pinpointOptions.get(LucyNetHeader.PINPOINT_PARENT_APPLICATION_NAME.toString());
            if (parentApplicationName != null) {
                final String host = pinpointOptions.get(LucyNetHeader.PINPOINT_HOST.toString());
                if (host != null) {
                    recorder.recordAcceptorHost(host);
                } else {
                    recorder.recordAcceptorHost(dstAddress);
                }

                final String type = pinpointOptions.get(LucyNetHeader.PINPOINT_PARENT_APPLICATION_TYPE.toString());
                final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(args)) {
            return;
        }

        if (recorder.canSampled()) {
            NpcMessage npcMessage = (NpcMessage) args[0];

            Object call = npcMessage.getPayload();
            if (call instanceof Call) {

                final String parameters = LucyNetUtils.getParameterAsString(((Call) call).getParameters(), 64, 512);
                if (parameters != null && parameters.length() > 0) {
                    recorder.recordAttribute(BlocConstants.CALL_PARAM, parameters);
                }
            }

            recorder.recordApi(methodDescriptor);
        }

        recorder.recordException(throwable);
    }

    private boolean validate(final Object[] args) {
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

        if (!(args[1] instanceof String)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], String.class.getName());
            }
            return false;
        }
        return true;
    }

}
