package com.navercorp.pinpoint.plugin.arcus.accessor;

import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

public interface CacheNameAccessor extends TraceValue {
    public String __getCacheName();
    public void __setCacheName(String cacheName);
}
