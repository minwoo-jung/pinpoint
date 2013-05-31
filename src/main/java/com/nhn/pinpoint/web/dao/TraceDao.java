package com.nhn.pinpoint.web.dao;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.common.bo.SpanBo;

/**
 *
 */
public interface TraceDao {

    List<SpanBo> selectSpan(TraceId traceId);

    List<SpanBo> selectSpanAndAnnotation(TraceId traceId);

    // TODO list하고 set하고 비교해서 하나 없애야 될듯 하다.
    List<List<SpanBo>> selectSpans(List<TraceId> traceIdList);
    
    List<List<SpanBo>> selectAllSpans(Collection<TraceId> traceIdSet);

    List<List<SpanBo>> selectSpans(Set<TraceId> traceIdSet);

    List<SpanBo> selectSpans(TraceId traceId);
    
    @Deprecated
    List<List<SpanBo>> selectSpansAndAnnotation(Set<TraceId> traceIdList);
}
