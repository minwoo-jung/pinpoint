package com.navercorp.pinpoint.profiler.modifier.redis.interceptor;

import java.util.Map;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * GatewayServer(nBase-ARC client) getResource() method interceptor
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayServerMethodInterceptor implements SimpleAroundInterceptor, TargetClassLoader {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!(target instanceof MapTraceValue) || result == null || !(result instanceof MapTraceValue)) {
            return;
        }

        // result - RedisCluster
        final Map<String, Object> gatewayServerTraceValue = ((MapTraceValue) target).__getTraceBindValue();
        if (gatewayServerTraceValue != null) {
            final Map<String, Object> traceValue = ((MapTraceValue) result).__getTraceBindValue();
            // copy to destinationId
            if (traceValue != null) {
                traceValue.put("destinationId", gatewayServerTraceValue.get("destinationId"));
            }
        }
    }
}