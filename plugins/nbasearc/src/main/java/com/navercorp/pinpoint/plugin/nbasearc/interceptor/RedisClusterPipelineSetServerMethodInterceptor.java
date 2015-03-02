package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * RedisCluster pipeline(nBase-ARC client) constructor interceptor 
 * - trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineSetServerMethodInterceptor implements SimpleAroundInterceptor, NbaseArcConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetadataAccessor destinationIdAccessor;
    private MetadataAccessor endPointAccessor;

    public RedisClusterPipelineSetServerMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor,
            @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        this.destinationIdAccessor = destinationIdAccessor;
        this.endPointAccessor = endPointAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(target, args)) {
            return;
        }

        // trace destinationId & endPoint
        // first arg is GatewayServer
        final GatewayServer server = (GatewayServer) args[0];
        final String endPoint = server.getAddress().getHost() + ":" + server.getAddress().getPort();
        endPointAccessor.set(target, endPoint);

        final String destinationId = destinationIdAccessor.get(args[0]);
        if (destinationId != null) {
            destinationIdAccessor.set(target, destinationId);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!destinationIdAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target. 'not apply metadata accessor, name={}'", METADATA_DESTINATION_ID);
            }
            return false;
        }

        if (!endPointAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target. 'not apply metadata accessor, name={}'", METADATA_END_POINT);
            }
            return false;
        }

        if (args == null || args.length == 0 || args[0] == null) {
            if (isDebug) {
                logger.debug("Invalid arguments. 'null or not found args={}'", args);
            }
            return false;
        }

        try {
            if (!(args[0] instanceof GatewayServer)) {
                if (isDebug) {
                    logger.debug("Invalid arguments. 'expect GatewayConfig, args[0]={}'", args[0]);
                }
                return false;
            }
        } catch (Exception e) {
            // expect 'class not found exception - GatewayServer'
            if (isDebug) {
                logger.debug("It does not support backward compatibility.", e);
            }
            return false;
        }

        if (!destinationIdAccessor.isApplicable(args[0])) {
            if (isDebug) {
                logger.debug("Invalid args[0]. 'not apply metadata accessor, name={}'", METADATA_DESTINATION_ID);
            }

            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}