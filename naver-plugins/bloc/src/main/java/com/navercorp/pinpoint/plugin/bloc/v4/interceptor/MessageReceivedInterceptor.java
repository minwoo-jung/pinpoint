package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.NpcMessage;
import external.org.apache.mina.common.IoSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;


public class MessageReceivedInterceptor extends AbstractBlocAroundInterceptor {

    private static final String NAMESPACE_URA = "URA 1.0";
    private static final String UNKNOWN_ADDRESS = "Unknown Address";

    public MessageReceivedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, MessageReceivedInterceptor.class);
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

        Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

        boolean isSampling = samplingEnable(pinpointOptions);
        if (!isSampling) {
            // Even if this transaction is not a sampling target, we have to
            // create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add
            // parameter to tell remote node 'don't sample this transaction'
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace remoteAddr:{}", new Object[]{ioSession.getRemoteAddress().toString()});
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(pinpointOptions);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, remoteAddr:{}", new Object[]{traceId, ioSession.getRemoteAddress().toString()});
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, remoteAddr:{}", traceId, ioSession.getRemoteAddress().toString());
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. remoteAddr:{}", ioSession.getRemoteAddress().toString());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. remoteAddr:{}", ioSession.getRemoteAddress().toString());
                }
            }
            return trace;
        }

    }

    private boolean samplingEnable(Map<String, String> pinpointOptions) {
        // optional value
        final String samplingFlag = pinpointOptions.get(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(Map<String, String> options) {
        String transactionId = options.get(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanID = NumberUtils.parseLong(options.get(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(options.get(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(options.get(Header.HTTP_FLAGS.toString()), (short) 0);

            // it can
            if (parentSpanID == SpanId.NULL || spanID == SpanId.NULL) {
                return null;
            }

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
        try {
            SpanRecorder spanRecorder = trace.getSpanRecorder();

            spanRecorder.recordServiceType(BlocConstants.BLOC);

            NpcMessage npcMessage = (NpcMessage) args[2];
            spanRecorder.recordRpcName(LucyNetUtils.getRpcName(npcMessage));

            final IoSession ioSession = (IoSession) args[1];
            spanRecorder.recordEndPoint(getLocalAddress(ioSession));
            spanRecorder.recordRemoteAddress(getRemoteAddress(ioSession));

            if (!spanRecorder.isRoot()) {
                Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

                String parentApplicationName = pinpointOptions.get(Header.HTTP_PARENT_APPLICATION_NAME.toString());
                if (parentApplicationName != null) {
                    final String remoteHost = pinpointOptions.get(Header.HTTP_HOST.toString());
                    if (StringUtils.hasLength(remoteHost)) {
                        spanRecorder.recordAcceptorHost(remoteHost);
                    } else {
                        spanRecorder.recordAcceptorHost(getLocalAddress(ioSession));
                    }

                    final String type = pinpointOptions.get(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
                    final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
                    spanRecorder.recordParentApplication(parentApplicationName, parentApplicationType);
                }
            }
            spanRecorder.recordApi(blocMethodApiTag);
        } finally {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordApi(methodDescriptor);
            spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
            getParams(args, spanEventRecorder);
        }
    }

    private String getRemoteAddress(IoSession ioSession) {
        if (ioSession == null) {
            return UNKNOWN_ADDRESS;
        }

        return getIpPort(ioSession.getRemoteAddress());
    }

    private String getLocalAddress(IoSession ioSession) {
        if (ioSession == null) {
            return UNKNOWN_ADDRESS;
        }

        return getIpPort(ioSession.getLocalAddress());
    }

    private String getIpPort(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return UNKNOWN_ADDRESS;
        }

        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) socketAddress;
            return HostAndPort.toHostAndPortString(addr.getAddress().getHostAddress(), addr.getPort());
        }

        String address = socketAddress.toString();
        int addressLength = address.length();

        if (addressLength > 0) {
            if (address.startsWith("/")) {
                return address.substring(1);
            } else {
                int delimiterIndex = address.indexOf('/');
                if (delimiterIndex != -1 && delimiterIndex < addressLength) {
                    return address.substring(address.indexOf('/') + 1);
                }
            }
        }

        return address;
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