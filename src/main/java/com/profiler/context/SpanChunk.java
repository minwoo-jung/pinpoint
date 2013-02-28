package com.profiler.context;

import com.profiler.Agent;
import com.profiler.common.dto.thrift.Event;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class SpanChunk implements Thriftable {

    private List<SubSpan> subSpanList = new ArrayList<SubSpan>();

    public SpanChunk(List<SubSpan> subSpanList) {
        this.subSpanList = subSpanList;
    }

    @Override
    public TBase toThrift() {
        com.profiler.common.dto.thrift.SpanChunk tSpanChunk = new com.profiler.common.dto.thrift.SpanChunk();
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        SubSpan first = subSpanList.get(0);
        Span parentSpan = first.getParentSpan();
        tSpanChunk.setAgentId(Agent.getInstance().getAgentId());
        tSpanChunk.setAgentIdentifier(Agent.getInstance().getIdentifier());

        UUID id = parentSpan.getTraceID().getId();
        tSpanChunk.setMostTraceId(id.getMostSignificantBits());
        tSpanChunk.setLeastTraceId(id.getLeastSignificantBits());
        tSpanChunk.setSpanId(parentSpan.getTraceID().getSpanId());

        List<Event> tSubSpan = createSubSpan(subSpanList);

        tSpanChunk.setSubSpanList(tSubSpan);

        return tSpanChunk;
    }

    private List<Event> createSubSpan(List<SubSpan> subSpanList) {
        List<Event> result = new ArrayList<Event>(subSpanList.size());
        for (SubSpan subSpan : subSpanList) {
            Event tSubSpan = new Event();

            tSubSpan.setAgentId(Agent.getInstance().getAgentId());
            tSubSpan.setAgentIdentifier(Agent.getInstance().getIdentifier());

            long parentSpanStartTime = subSpan.getParentSpan().getStartTime();
            tSubSpan.setStartElapsed((int) (subSpan.getStartTime() - parentSpanStartTime));
            tSubSpan.setEndElapsed((int) (subSpan.getEndTime() - subSpan.getStartTime()));

            tSubSpan.setSequence(subSpan.getSequence());

            tSubSpan.setRpc(subSpan.getRpc());
//            tSubSpan.setServiceName(subSpan.getServiceName());
            tSubSpan.setServiceType(subSpan.getServiceType().getCode());
            tSubSpan.setDestinationId(subSpan.getDestionationId());

            tSubSpan.setEndPoint(subSpan.getEndPoint());
            tSubSpan.setDestinationId(subSpan.getDestionationId());

            // 여기서 데이터 인코딩을 하자.
            List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(subSpan.getAnnotationSize());
            for (Annotation a : subSpan.getAnnotations()) {
                annotationList.add(a.toThrift());
            }
            
			if (subSpan.getDepth() != -1) {
				tSubSpan.setDepth(subSpan.getDepth());
			}

			if (subSpan.getNextSpanId() != -1) {
				tSubSpan.setNextSpanId(subSpan.getNextSpanId());
			}
            
            tSubSpan.setAnnotations(annotationList);
            result.add(tSubSpan);
        }
        return result;
    }
}
