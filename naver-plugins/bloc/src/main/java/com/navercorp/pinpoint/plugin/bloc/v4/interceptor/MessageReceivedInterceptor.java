package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
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
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.nhncorp.lucy.npc.NpcMessage;
import external.org.apache.mina.common.IoSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;


@TargetMethod(name = "messageReceived", paramTypes = {"external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object"})
public class MessageReceivedInterceptor extends SpanSimpleAroundInterceptor {

    private static final String NAMESPACE_URA = "URA 1.0";
    private static final String UNKNOWN_ADDRESS = "Unknown Address";

    public MessageReceivedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, MessageReceivedInterceptor.class);
    }

    private boolean validate(final Object[] args) {
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
        if (!validate(args)) {
            return null;
        }

        IoSession ioSession = getIOSession(args);
        NpcMessage npcMessage = getNpcMessage(args);

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

    @Override
    public void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        NpcMessage npcMessage = getNpcMessage(args);

        recorder.recordServiceType(BlocConstants.BLOC);
        recorder.recordRpcName(LucyNetUtils.getRpcName(npcMessage));

        final IoSession ioSession = getIOSession(args);
        recorder.recordEndPoint(getLocalAddress(ioSession));
        recorder.recordRemoteAddress(getRemoteAddress(ioSession));

        if (!recorder.isRoot()) {
            Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);

            String parentApplicationName = pinpointOptions.get(Header.HTTP_PARENT_APPLICATION_NAME.toString());
            if (parentApplicationName != null) {
                String remoteHost = pinpointOptions.get(Header.HTTP_HOST.toString());
                if (remoteHost != null && remoteHost.length() > 0) {
                    recorder.recordAcceptorHost(remoteHost);
                } else {
                    recorder.recordAcceptorHost(getLocalAddress(ioSession));
                }

                final String type = pinpointOptions.get(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
                final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(args)) {
            return;
        }

        if (recorder.canSampled()) {
            recorder.recordApi(methodDescriptor);
        }

        recorder.recordException(throwable);
    }

    private IoSession getIOSession(Object[] args) {
        if (args.length >= 2) {
            if (args[1] instanceof external.org.apache.mina.common.IoSession) {
                return (IoSession) args[1];
            }
        }
        return null;
    }

    private NpcMessage getNpcMessage(Object[] args) {
        if (args.length >= 3) {
            if (args[2] instanceof NpcMessage) {
                return (NpcMessage) args[2];
            }
        }
        return null;
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
            return addr.getAddress().getHostAddress() + ":" + addr.getPort();
        }

        String address = socketAddress.toString();
        int addressLength = address.length();

        if (addressLength > 0) {
            if (address.startsWith("/")) {
                return address.substring(1);
            } else {
                int delimeterIndex = address.indexOf('/');
                if (delimeterIndex != -1 && delimeterIndex < addressLength) {
                    return address.substring(address.indexOf('/') + 1);
                }
            }
        }

        return address;
    }

}