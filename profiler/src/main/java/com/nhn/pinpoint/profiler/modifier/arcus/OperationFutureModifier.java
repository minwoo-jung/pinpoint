package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class OperationFutureModifier extends AbstractFutureModifier {


    public OperationFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String getTargetClass() {
        return "net/spy/memcached/internal/OperationFuture";
    }
}
