package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.NpcServerAddressAccessor;
import com.nhncorp.lucy.npc.DefaultNpcMessage;
import com.nhncorp.lucy.npc.UserOptionIndex;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class MakeMessageInterceptor implements AroundInterceptor {

    private static final int DEFAULT_MAX_USER_OPTIONS_SET_INDEX = 3;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public MakeMessageInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null || !trace.canSampled()) {
            return;
        }
        
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        TraceId id = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(id.getSpanId());
        if (result instanceof com.nhncorp.lucy.npc.DefaultNpcMessage) {
            recorder.recordServiceType(LucyNetConstants.NPC_CLIENT);
            String endPoint = LucyNetConstants.UNKOWN_ADDRESS;
            if (target != null && target instanceof NpcServerAddressAccessor) {
                InetSocketAddress serverAddress = ((NpcServerAddressAccessor) target)._$PINPOINT$_getNpcServerAddress();
                if (serverAddress != null) {
                    int port = serverAddress.getPort();
                    endPoint = getHostAddress(serverAddress) + ((port > 0) ? ":" + port : "");
                }
            }
            recorder.recordDestinationId(endPoint);

            com.nhncorp.lucy.npc.DefaultNpcMessage defaultNpcMessage = (com.nhncorp.lucy.npc.DefaultNpcMessage) result;
            Map<String, Object> options = createOption(id, endPoint);
            putOption(defaultNpcMessage, options);
        } else {
            recorder.recordDestinationId(LucyNetConstants.UNKOWN_ADDRESS);
        }
    }

    private String getHostAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        // TODO JDK 1.7 InetSocketAddress.getHostString();
        // Warning : Avoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    private Map<String, Object> createOption(TraceId id, String endPoint) {
        Map<String, Object> options = new HashMap<String, Object>();
        
        options.put(Header.HTTP_TRACE_ID.toString(), id.getTransactionId());
        options.put(Header.HTTP_SPAN_ID.toString(), id.getSpanId());
        options.put(Header.HTTP_PARENT_SPAN_ID.toString(), id.getParentSpanId());
        options.put(Header.HTTP_FLAGS.toString(), String.valueOf(id.getFlags()));
        options.put(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
        options.put(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), traceContext.getServerTypeCode());
        options.put(Header.HTTP_HOST.toString(), endPoint);
        return options;
    }
    
    private boolean putOption(DefaultNpcMessage npcMessage, Map<String, Object> options) {
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            if (isEmpty(entry.getKey())) {
                return false;
            }
            if (isEmpty(entry.getValue())) {
                return false;
            }

            String option = entry.getKey() + "=" + entry.getValue();
            UserOptionIndex optionIndex = findAvaiableOptionIndex(npcMessage);
            if (optionIndex == null) {
                return false;
            }
            
            npcMessage.setUserOption(optionIndex, option.getBytes());
        }
        return true;
    }
    
    private UserOptionIndex findAvaiableOptionIndex(DefaultNpcMessage npcMessage) {
        return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(1, 0), DEFAULT_MAX_USER_OPTIONS_SET_INDEX);
    }

    private UserOptionIndex findAvaiableOptionIndex(DefaultNpcMessage npcMessage, UserOptionIndex optionIndex, int maxUserOptionSetIndex) {
        int optionSetIndex = optionIndex.getOptionSetIndex();

        if (optionSetIndex == maxUserOptionSetIndex) {
            return null;
        }
        
        byte[] data = npcMessage.getUserOption(optionIndex);
        if (data == null) {
            return optionIndex;
        }

        int flagIndex = optionIndex.getFlagIndex() + 1;
        if (flagIndex == 32) {
            return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(optionSetIndex + 1, 0), maxUserOptionSetIndex);
        } else {
            return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(optionSetIndex, flagIndex), maxUserOptionSetIndex);
        }
    }
    
    private boolean isEmpty(Object value) {
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        return false;
    }
    
    private String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        List<String> ipList = NetUtils.getLocalV4IpList();
        if (ipList.size() > 0) {
            return ipList.get(0);
        }

        return NetUtils.LOOPBACK_ADDRESS_V4;
    }

}
