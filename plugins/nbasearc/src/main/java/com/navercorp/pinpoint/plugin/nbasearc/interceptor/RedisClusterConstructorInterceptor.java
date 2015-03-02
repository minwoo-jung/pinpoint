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
 * RedisCluster(nBase-ARC client) constructor interceptor - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterConstructorInterceptor implements SimpleAroundInterceptor, NbaseArcConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetadataAccessor endPointAccessor;
    
    public RedisClusterConstructorInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
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

        // trace endPoint
        // first arg - host
        final StringBuilder endPoint = new StringBuilder();
        if (args[0] instanceof String) {
            endPoint.append(args[0]);
            // second arg - port
            if (args.length >= 2 && args[1] != null && args[1] instanceof Integer) {
                endPoint.append(":").append(args[1]);
            } else {
                // default port
                endPoint.append(":").append(6379);
            }
        }

        endPointAccessor.set(target, endPoint.toString());
    }

    private boolean validate(final Object target, final Object[] args) {
        if(args == null || args.length == 0 || args[0] == null) {
            logger.debug("Invalid arguments. 'null or not found args={}'", args);
            return false;
        }
        
        if(!endPointAccessor.isApplicable(target)) {
            logger.debug("Invalid target. 'not apply metadata accessor, name={}'", METADATA_END_POINT);
            return false;
        }
        
        return true;
    }
    
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
