package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.LoggingDataSender;

/**
 *
 */
public class MockTraceContextFactory {
    public TraceContext create() {
        DefaultTraceContext traceContext = new DefaultTraceContext();
        SpanStorageFactory spanStorageFactory = new SpanStorageFactory(new LoggingDataSender());
        traceContext.setStorageFactory(spanStorageFactory);
        return traceContext;
    }
}
