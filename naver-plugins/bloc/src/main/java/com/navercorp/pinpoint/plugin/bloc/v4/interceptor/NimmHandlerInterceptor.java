package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.plugin.bloc.AbstractBlocAroundInterceptor;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.LucyNetServerRequestWrapper;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.NpcMessage;

import java.util.Map;

/**
 * @author Taejin Koo
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

        if (!(args[1] instanceof String)) {
            if (isDebug) {
                logger.debug("Invalid args[1]={}. Need {}", args[1], String.class.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        NpcMessage npcMessage = (NpcMessage) args[0];

        Map<String, String> pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);
        final String rpcName = LucyNetUtils.getRpcName(npcMessage);
        String remoteAddress = (String) args[1];
        String dstAddress = BlocConstants.UNKOWN_ADDRESS;
        if (target instanceof NimmServerSocketAddressAccessor) {
            dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }
        final String endPoint = dstAddress;

        final ServerRequestWrapper serverRequestWrapper = new LucyNetServerRequestWrapper(pinpointOptions, rpcName, endPoint, remoteAddress, endPoint);
        final Trace trace = this.requestTraceReader.read(serverRequestWrapper);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(BlocConstants.BLOC);
            spanRecorder.recordApi(blocMethodApiTag);
            serverRequestRecorder.record(spanRecorder, serverRequestWrapper);
        }
        return trace;
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object target, Object[] args) {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordApi(methodDescriptor);
        spanEventRecorder.recordServiceType(BlocConstants.BLOC_INTERNAL_METHOD);
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