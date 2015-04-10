package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

public class InvokeInterceptor implements SimpleAroundInterceptor, LucyNetConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final MetadataAccessor serverAddressAccessor;
    
    public InvokeInterceptor(TraceContext traceContext, @Cached MethodDescriptor descriptor, @Name(METADATA_NPC_SERVER_ADDRESS) MetadataAccessor serverAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.serverAddressAccessor = serverAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

//        Trace trace = traceContext.currentRawTraceObject();
        // 이부분은 현재 remote 호출시 traceId를 넣지 않으므로 currentRawTraceObject()를 호출하지 않는다.
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        String objectName;
        String methodName;
        Charset charset;
        Object params;

        if (args.length == 3) {
            objectName = (String) args[0];
            methodName = (String) args[1];
            // TODO charset을 com.nhncorp.lucy.npc.connector.AbstractConnector.getDefaultCharset() 에서 조회 가능하긴 함.
            charset = null;
            params = args[2];
        } else if (args.length == 4) {
            objectName = (String) args[0];
            methodName = (String) args[1];
            charset = (Charset) args[2];
            params = args[3];
        }

        //
        // TODO add sampling logic here.
        //

        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        trace.recordNextSpanId(nextId.getSpanId());

        //
        // TODO add pinpoint headers to the request message here.
        //

        trace.recordServiceType(NPC_CLIENT);

        InetSocketAddress serverAddress = serverAddressAccessor.get(target);
        int port = serverAddress.getPort();
        String endPoint = serverAddress.getHostName() + ((port > 0) ? ":" + port : "");
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endPoint);
        trace.recordDestinationId(endPoint);

        trace.recordAttribute(NPC_URL, serverAddress.toString());
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            trace.recordApi(descriptor);
            trace.recordException(throwable);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}