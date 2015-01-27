package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.arcus.ArcusMetadata;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SetCacheManagerInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        String serviceCode = ArcusMetadata.SERVICE_CODE.get(args[0]);
        ArcusMetadata.SERVICE_CODE.set(target, serviceCode);
    }
}
