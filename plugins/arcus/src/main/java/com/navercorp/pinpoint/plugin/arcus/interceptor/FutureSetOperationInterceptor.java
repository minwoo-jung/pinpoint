package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.bootstrap.plugin.TargetMethod;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;


/**
 * @author harebox
 * @author emeroad
 */
@TargetMethod(name="setOperation", paramTypes="net.spy.memcached.ops.Operation")
public class FutureSetOperationInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final MetadataAccessor operationAccessor;
    
    public FutureSetOperationInterceptor(@Name(METADATA_OPERATION) MetadataAccessor operationAccessor) {
        this.operationAccessor = operationAccessor;
    }


    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        operationAccessor.set(target, args[0]);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }
}
