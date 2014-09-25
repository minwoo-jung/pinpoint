package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;

/**
 * @author emeroad
 */
public interface TraceFactory {
    Trace currentTraceObject();

    Trace currentRpcTraceObject();

    Trace currentRawTraceObject();

    Trace disableSampling();

    // remote 에서 샘플링 대상으로 선정된 경우.
    Trace continueTraceObject(TraceId traceID);

    Trace newTraceObject();

    void detachTraceObject();
}
