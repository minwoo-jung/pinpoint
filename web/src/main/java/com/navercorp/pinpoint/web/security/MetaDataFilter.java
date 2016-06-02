package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;

public interface MetaDataFilter {
    
    public enum MetaData {
        API, SQL, PARAM
    }

    boolean filter(SpanAlign spanAlign, MetaData metaData);

    AnnotationBo createAnnotationBo(SpanAlign spanAlign, MetaData metaData);

    Record createRecord(CallTreeNode node, RecordFactory factory);

    void replaceAnnotationBo(SpanAlign align, MetaData param);
}
