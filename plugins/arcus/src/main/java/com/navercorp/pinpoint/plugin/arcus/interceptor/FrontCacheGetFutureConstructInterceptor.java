package com.navercorp.pinpoint.plugin.arcus.interceptor;

import net.sf.ehcache.Element;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.arcus.ArcusMetadata;

/**
 * @author harebox
 */
public class FrontCacheGetFutureConstructInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
        
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            // set cacheName
            ArcusMetadata.CACHE_NAME.set(target, DEFAULT_FRONTCACHE_NAME);
            
            if (args[0] instanceof Element) {
                Element element = (Element) args[0];
                // set cacheKey
                ArcusMetadata.CACHE_KEY.set(target, element.getObjectKey());
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }
}
