package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.LucyNetServerRequestTrace;
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
        final String rpcName = LucyNetUtils.getRpcName(npcMessage);
        final String endPoint = getLocalAddress(ioSession);
        final String remoteAddress = getRemoteAddress(ioSession);

        final ServerRequestTrace serverRequestTrace = new LucyNetServerRequestTrace(pinpointOptions, rpcName, endPoint, remoteAddress, endPoint);
        final Trace trace = this.requestTraceReader.read(serverRequestTrace);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            this.serverRequestRecorder.record(spanRecorder, serverRequestTrace);
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