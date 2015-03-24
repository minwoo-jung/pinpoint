package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.TargetMethod;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

import external.org.apache.mina.common.IoSession;


@TargetMethod(name="messageReceived", paramTypes={"external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object"})
public class MessageReceivedInterceptor extends SpanSimpleAroundInterceptor implements BlocConstants {

    public MessageReceivedInterceptor() {
        super(MessageReceivedInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
        
        if (trace.canSampled()) {
            trace.recordServiceType(BLOC);
            trace.recordRpcName("NPC Call");

            final IoSession ioSession = (IoSession)args[1];
            trace.recordEndPoint(ioSession.getLocalAddress().toString());
            trace.recordRemoteAddress(ioSession.getRemoteAddress().toString());
        }

    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final Trace trace = getTraceContext().newTraceObject();
        
        if (isDebug) {
            final IoSession ioSession = (IoSession)args[1];
            
            if (trace.canSampled()) {
                logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[] {ioSession.getRemoteAddress().toString()});
            } else {
                logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[] {ioSession.getRemoteAddress().toString()});
            }
        }
        
        return trace;
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordApi(getMethodDescriptor());
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}
