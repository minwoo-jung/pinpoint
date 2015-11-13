package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.NpcMessage;
import com.nhncorp.lucy.npc.UserOptionIndex;

import external.org.apache.mina.common.IoSession;


@TargetMethod(name="messageReceived", paramTypes={"external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object"})
public class MessageReceivedInterceptor extends SpanSimpleAroundInterceptor {

    private static final String NAMESPACE_URA = "URA 1.0";
    private static final String UNKNOWN_ADDRESS = "Unknown Address";
    
    public MessageReceivedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, MessageReceivedInterceptor.class);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        IoSession ioSession = getIOSession(args);
        NpcMessage npcMessage = getNpcMessage(args);

        List<String> userOptions = getAllUserOptions(npcMessage);
        Map<String, String> pinpointOptions = extractPinpointOptions(userOptions);

        boolean isSampling = samplingEnable(pinpointOptions);
        if (!isSampling) {
            // Even if this transaction is not a sampling target, we have to
            // create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add
            // parameter to tell remote node 'don't sample this transaction'
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace remoteAddr:{}", new Object[] { ioSession.getRemoteAddress().toString() });
            }
            return trace;
        }
        
        final TraceId traceId = populateTraceIdFromRequest(pinpointOptions);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, remoteAddr:{}", new Object[] { traceId, ioSession.getRemoteAddress().toString() });
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, remoteAddr:{}", new Object[] { traceId,
                            ioSession.getRemoteAddress().toString() });
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. remoteAddr:{}", new Object[] { ioSession.getRemoteAddress().toString() });
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. remoteAddr:{}", new Object[] { ioSession.getRemoteAddress().toString() });
                }
            }
            return trace;
        }
        
    }

    @Override
    public void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        NpcMessage npcMessage = getNpcMessage(args);

        recorder.recordServiceType(BlocConstants.BLOC);
        recorder.recordRpcName(createRpcName(npcMessage));

        final IoSession ioSession = getIOSession(args);
        recorder.recordEndPoint(getLocalAddress(ioSession));
        recorder.recordRemoteAddress(getRemoteAddress(ioSession));

        if (!recorder.isRoot()) {
            List<String> userOptions = getAllUserOptions(npcMessage);
            Map<String, String> pinpointOptions = extractPinpointOptions(userOptions);

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
    
    private String createRpcName(NpcMessage npcMessage) {
        // use string builder

        if (npcMessage == null) {
            return "//";
        }

        StringBuilder rpcNameBuilder = new StringBuilder("/");

        String namespace = trimToEmpty(npcMessage.getNamespace());
        String objectName = getObjectName(npcMessage.getPayload());
        String methodName = getMethodName(npcMessage.getPayload());

        if (namespace.equals(NAMESPACE_URA)) {
            rpcNameBuilder.append(methodName);
        } else {
            rpcNameBuilder.append(objectName);
            rpcNameBuilder.append('/');
            rpcNameBuilder.append(methodName);
        }

        return rpcNameBuilder.toString();
    }
    
    private String getObjectName(Object payload) {
        if (payload == null || !(payload instanceof Call)) {
            return "";
        }

        Call call = (Call) payload;
        return trimToEmpty(call.getObjectName());
    }
    
    private String getMethodName(Object payload) {
        if (payload == null || !(payload instanceof Call)) {
            return "";
        }

        Call call = (Call) payload;
        return trimToEmpty(call.getMethodName());
    }
    
    private String trimToEmpty(String value) {
        if (value == null || value.length() == 0) {
            return "";
        } else {
            return value;
        }
    }

    private List<String> getAllUserOptions(NpcMessage npcMessage) {
        return getAllOptions(npcMessage, npcMessage.getUserOptionCount(), npcMessage.getUserOptionFlagCount());
    }
    
    private List<String> getAllOptions(NpcMessage npcMessage, int optionSetCount, int optionsCount) {
        // Null Pointer 날수있음
        List<String> options = new ArrayList<String>(optionsCount);

        for (int i = 1; i <= optionSetCount; i++) {
            List<String> specificSetOptions = getSpecificSetOptions(npcMessage, i);

            options.addAll(specificSetOptions);

            if (options.size() == optionsCount) {
                return options;
            }
        }

        return options;
    }
    
    private List<String> getSpecificSetOptions(NpcMessage message, int optionSetIndex) {
        int maxOptionSize = 32;

        List<String> options = new ArrayList<String>();

        for (int i = 0; i < maxOptionSize; i++) {
            UserOptionIndex optionIndex = new UserOptionIndex(optionSetIndex, i);

            try {
                byte[] value = message.getUserOption(optionIndex);
                if (value != null) {
                    String option = new String(value);
                    options.add(option);
                }
            } catch (Exception e) {
            }
        }

        return options;
    }
    
    private Map<String, String> extractPinpointOptions(List<String> userOptions) {
        Map<String, String> pinpointOptions = new HashMap<String, String>();

        for (String userOption : userOptions) {
            String[] keyValuePair = userOption.split("=");

            if (keyValuePair.length == 2) {
                String key = keyValuePair[0];
                String value = keyValuePair[1];

                boolean isPinpointHeader = Header.hasHeader(key);
                if (isPinpointHeader) {
                    pinpointOptions.put(key, value);
                }
            }
        }

        return pinpointOptions;
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
                int delimeterIndex = address.indexOf("/");
                if (delimeterIndex != -1 && delimeterIndex < addressLength) {
                    return address.substring(address.indexOf("/") + 1);
                }
            }
        }

        return address;
    }
    
    
}