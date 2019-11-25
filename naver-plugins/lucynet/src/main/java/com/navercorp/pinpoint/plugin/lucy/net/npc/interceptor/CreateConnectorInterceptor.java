package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

import com.nhncorp.lucy.npc.connector.NpcConnectorOption;

import java.net.InetSocketAddress;

public class CreateConnectorInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public CreateConnectorInterceptor(MethodDescriptor descriptor, TraceContext traceContext) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT_INTERNAL);

        InetSocketAddress serverAddress = null;
        if (ArrayUtils.hasLength(args) && args[0] instanceof NpcConnectorOption) {
            NpcConnectorOption option = (NpcConnectorOption) args[0];
            serverAddress = option.getAddress();
        }

        final String endPoint = getEndPoint(serverAddress);
        recorder.recordAttribute(LucyNetConstants.NPC_URL, endPoint);
    }

    private String getEndPoint(InetSocketAddress serverAddress) {
        if (serverAddress != null) {
            return EndPointUtils.getEndPoint(serverAddress);
        } else {
            return "unknown";
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

}