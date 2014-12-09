package com.navercorp.pinpoint.plugin.arcus.accessor;

import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

public interface ServiceCodeAccessor extends TraceValue {
    public void __setServiceCode(String serviceCode);
    public String __getServiceCode();
}
