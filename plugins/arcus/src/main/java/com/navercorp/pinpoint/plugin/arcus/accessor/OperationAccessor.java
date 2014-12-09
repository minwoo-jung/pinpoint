package com.navercorp.pinpoint.plugin.arcus.accessor;

import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

public interface OperationAccessor extends TraceValue {
    public Operation __getOperation();
    public void __setOperation(Operation operation);
}
