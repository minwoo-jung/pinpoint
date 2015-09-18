package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory;

@TargetConstructor
public class ModuleClassLoaderFactoryInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
        
    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object result, Throwable throwable, Object[] args) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        
        ModuleClassLoaderFactory moduleClassLoaderFactory = (ModuleClassLoaderFactory)target;
        moduleClassLoaderFactory.addManagedPackage("com.navercorp.pinpoint.bootstrap");
        moduleClassLoaderFactory.addManagedPackage("com.navercorp.pinpoint.common");
        
    }

}
