package com.nhn.pinpoint.web.dao;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.common.bo.SpanBo;

/**
 *
 */
public interface TraceDao {

    List<SpanBo> selectSpan(TransactionId traceId);

    List<SpanBo> selectSpanAndAnnotation(TransactionId traceId);

    // TODO list하고 set하고 비교해서 하나 없애야 될듯 하다.
    List<List<SpanBo>> selectSpans(List<TransactionId> traceIdList);
    
    List<List<SpanBo>> selectAllSpans(Collection<TransactionId> traceIdSet);

    List<List<SpanBo>> selectSpans(Set<TransactionId> traceIdSet);

    List<SpanBo> selectSpans(TransactionId traceId);
    
    @Deprecated
    List<List<SpanBo>> selectSpansAndAnnotation(Set<TransactionId> traceIdList);
}
