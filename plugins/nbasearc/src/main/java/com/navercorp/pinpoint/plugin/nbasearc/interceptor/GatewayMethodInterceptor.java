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

/**
 * Gateway(nBase-ARC client) getServer() method interceptor - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayMethodInterceptor implements SimpleAroundInterceptor, NbaseArcConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetadataAccessor destinationIdAccessor;

    public GatewayMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor) {
        this.destinationIdAccessor = destinationIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(target, result)) {
            return;
        }

        final String destinationId = destinationIdAccessor.get(target);
        if (destinationId != null) {
            // result is GatewayServer object
            destinationIdAccessor.set(result, destinationId);
        }
    }

    private boolean validate(final Object target, final Object result) {
        if (result == null) {
            logger.debug("Skip result. 'null'");
            return false;
        }

        if (!destinationIdAccessor.isApplicable(target)) {
            logger.debug("Invalid target. 'not apply metadata accessor, name={}'", METADATA_DESTINATION_ID);
            return false;
        }

        if (!destinationIdAccessor.isApplicable(result)) {
            logger.debug("Invalid result. 'not apply metadata accessor, name={}'", METADATA_DESTINATION_ID);
            return false;
        }

        return true;
    }
}