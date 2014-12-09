package com.navercorp.pinpoint.profiler.modifier.redis.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * RedisCluster pipeline(nBase-ARC client) constructor interceptor - trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!(target instanceof MapTraceValue)) {
            return;
        }

        // trace destinationId & endPoint
        final Map<String, Object> traceValue = new HashMap<String, Object>();

        // first arg : GatewayServer
        try {
            final GatewayServer server = (GatewayServer) args[0];
            traceValue.put("endPoint", server.getAddress().getHost() + ":" + server.getAddress().getPort());
        } catch (Exception e) {
            // expect 'class not found exception - GatewayServer'
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to trace endPoint('not found GatewayServer' is compatibility error). caused={}", e.getMessage(), e);
            }
        }

        if (args[0] instanceof MapTraceValue) {
            final Map<String, Object> gatewayServerTraceValue = ((MapTraceValue) args[0]).__getTraceBindValue();
            if (gatewayServerTraceValue != null) {
                traceValue.put("destinationId", gatewayServerTraceValue.get("destinationId"));
            }
        }

        ((MapTraceValue) target).__setTraceBindValue(traceValue);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
