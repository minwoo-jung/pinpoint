package com.navercorp.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * Gateway(nBase-ARC client) getServer() method interceptor
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayMethodInterceptor implements SimpleAroundInterceptor, TargetClassLoader {
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

        // result - GatewayServer
        final Map<String, Object> gatewayTraceValue = ((MapTraceValue) target).__getTraceBindValue();
        if (gatewayTraceValue != null) {
            final Map<String, Object> traceValue = new HashMap<String, Object>();
            // copy to destinationId
            traceValue.put("destinationId", gatewayTraceValue.get("destinationId"));
            ((MapTraceValue) result).__setTraceBindValue(traceValue);
        }
    }
}