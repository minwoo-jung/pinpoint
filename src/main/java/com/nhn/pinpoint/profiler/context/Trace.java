package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;

import java.util.List;

/**
 *
 */
public interface Trace {

    public static final int DEFAULT_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    @Deprecated
    AsyncTrace createAsyncTrace();

    void traceBlockBegin();

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    void traceBlockBegin(int stackId);

    void traceRootBlockEnd();

    void traceBlockEnd();

    void traceBlockEnd(int stackId);

    TraceId getTraceId();

    boolean canSampled();


    void recordException(Object result);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end);

    void recordApi(int apiId);

    void recordApi(int apiId, Object[] args);

    void recordApi(int apiId, Object[] args, int start, int end);

    void recordAttribute(AnnotationKey key, String value);

    ParsingResult recordSqlInfo(String sql);

    void recordSqlParsingResult(ParsingResult parsingResult);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordDestinationId(String destinationId);

    void recordEndPoint(String endPoint);

    void recordRemoteAddress(String remoteAddress);

    void recordNextSpanId(int spanId);

    void recordParentApplication(String parentApplicationName, short parentApplicationType);

    void recordAcceptorHost(String host);

    int getStackFrameId();
}
