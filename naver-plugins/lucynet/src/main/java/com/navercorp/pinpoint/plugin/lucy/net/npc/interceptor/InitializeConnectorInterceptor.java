package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.lucy.net.EndPointUtils;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;

import java.net.InetSocketAddress;

public class InitializeConnectorInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public InitializeConnectorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(LucyNetConstants.NPC_CLIENT_INTERNAL);

        InetSocketAddress serverAddress = null;
        if (target instanceof NpcServerAddressAccessor) {
            serverAddress = ((NpcServerAddressAccessor) target)._$PINPOINT$_getNpcServerAddress();
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